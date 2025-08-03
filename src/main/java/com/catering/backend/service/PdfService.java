package com.catering.backend.service;

import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.repository.CommandeRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService {

    @Autowired
    private CommandeRepository commandeRepository;

    private Font getCalibriFont(float size, int style) {
        try {
            BaseFont baseFont = BaseFont.createFont("src/main/resources/fonts/calibri.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style);
        } catch (Exception e) {
            e.printStackTrace();
            return FontFactory.getFont(FontFactory.HELVETICA, size, style);
        }
    }

    public byte[] genererFicheCommande(Long commandeId) throws Exception {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new Exception("Commande introuvable"));

        List<ProduitCommande> produitsCoches = commande.getProduits().stream()
                .filter(ProduitCommande::isSelectionne)
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 41, 41, 70, 100);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(new HeaderFooterEvent());

        document.open();

        String dateFiche = (commande.getDateFiche() != null)
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(commande.getDateFiche()))
                : "??/??/????";

        PdfPTable headerLine = new PdfPTable(3);
        headerLine.setWidthPercentage(100);
        headerLine.setWidths(new float[]{4f, 1f, 4f});

        Font calibri16 = getCalibriFont(16, Font.NORMAL);

        PdfPCell leftCell = new PdfPCell(new Phrase("Fiche technique N° " + commande.getNumeroCommande(), calibri16));
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        leftCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell spaceCell = new PdfPCell(new Phrase(""));
        spaceCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell rightCell = new PdfPCell(new Phrase("Rabat le : " + dateFiche, calibri16));
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setBorder(Rectangle.NO_BORDER);

        headerLine.addCell(leftCell);
        headerLine.addCell(spaceCell);
        headerLine.addCell(rightCell);
        document.add(headerLine);

        Font calibri11 = getCalibriFont(11, Font.NORMAL);
        Font calibri11Bold = getCalibriFont(11, Font.BOLD);

        PdfPTable infoLine = new PdfPTable(4);
        infoLine.setWidthPercentage(100);
        infoLine.setWidths(new float[]{3f, 3.3f, 3.3f, 3f});

        Font calibri12 = getCalibriFont(12, Font.NORMAL);

        PdfPCell cellClient = new PdfPCell(new Phrase("Client : " + commande.getNomClient(), calibri12));
        PdfPCell cellNbr = new PdfPCell(new Phrase("Nbre de " +
                (commande.getTypeClient().name().equals("ENTREPRISE") ? "personnes" : "tables") +
                " : " + commande.getNombreTables(), calibri12));
        PdfPCell cellDate = new PdfPCell(new Phrase("Date : " + commande.getDate(), calibri12));
        PdfPCell cellSalle = new PdfPCell(new Phrase("Salle : " + commande.getSalle(), calibri12));

        cellClient.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNbr.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellDate.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellSalle.setHorizontalAlignment(Element.ALIGN_RIGHT);

        for (PdfPCell cell : List.of(cellClient, cellNbr, cellDate, cellSalle)) {
            cell.setBorder(Rectangle.NO_BORDER);
        }

        infoLine.addCell(cellClient);
        infoLine.addCell(cellNbr);
        infoLine.addCell(cellDate);
        infoLine.addCell(cellSalle);

        document.add(infoLine);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 2, 2});

        Color beige = new Color(221, 217, 195);
        String[] headers = {"Désignation", "Quantité", "PU (DH)", "Total"};
        for (int i = 0; i < headers.length; i++) {
            PdfPCell headerCell = new PdfPCell(new Phrase(headers[i], calibri11Bold));
            headerCell.setBackgroundColor(beige);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setBorder(Rectangle.BOX);
            headerCell.setBorderWidth(0.5f);
            table.addCell(headerCell);
        }

        Map<String, List<ProduitCommande>> produitsParCategorie = new LinkedHashMap<>();
        for (ProduitCommande produit : produitsCoches) {
            produitsParCategorie.computeIfAbsent(produit.getCategorie(), k -> new java.util.ArrayList<>()).add(produit);
        }

        String derniereCategorieNormale = produitsParCategorie.keySet().stream()
                .filter(c -> !c.equalsIgnoreCase("Supplément"))
                .reduce((first, second) -> second)
                .orElse(null);

        double totalSuppl = 0;
        int totalProduitsNormaux = produitsParCategorie.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase("Supplément"))
                .mapToInt(e -> e.getValue().size())
                .sum();
        int indexAffichageQte = totalProduitsNormaux / 2;
        int currentIndex = 0;

        String valeurQuantiteGlobale = String.valueOf(commande.getNombreTables());
        String valeurPU = String.format("%.2f", commande.getPrixParTable());
        String valeurTotal = String.format("%.2f", commande.getNombreTables() * commande.getPrixParTable());

        for (Map.Entry<String, List<ProduitCommande>> entry : produitsParCategorie.entrySet()) {
            String categorie = entry.getKey();
            List<ProduitCommande> produits = entry.getValue();

            boolean isSupplement = categorie.equalsIgnoreCase("Supplément");

            if (!isSupplement) {
                PdfPCell sectionDesignation = new PdfPCell(new Phrase(categorie + " :", getCalibriFont(12, Font.BOLD)));
                PdfPCell sectionQte = new PdfPCell();
                PdfPCell sectionPU = new PdfPCell();
                PdfPCell sectionTotal = new PdfPCell();

                int borderStyle = Rectangle.LEFT | Rectangle.RIGHT;
                sectionDesignation.setBorder(borderStyle);
                sectionQte.setBorder(borderStyle);
                sectionPU.setBorder(borderStyle);
                sectionTotal.setBorder(borderStyle);

                table.addCell(sectionDesignation);
                table.addCell(sectionQte);
                table.addCell(sectionPU);
                table.addCell(sectionTotal);
            }

            for (ProduitCommande produit : produits) {
                PdfPCell cellDesignation = new PdfPCell(new Phrase(" ‐ " + produit.getNom(), calibri11));
                PdfPCell cellQte;
                PdfPCell cellPU = new PdfPCell();
                PdfPCell cellTotal = new PdfPCell();

                if (!isSupplement) {
                    if (currentIndex == indexAffichageQte) {
                        cellQte = new PdfPCell(new Phrase(valeurQuantiteGlobale, calibri11));
                        cellQte.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellPU.setPhrase(new Phrase(valeurPU, calibri11));
                        cellPU.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellTotal.setPhrase(new Phrase(valeurTotal, calibri11));
                        cellTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        cellQte = new PdfPCell(new Phrase(""));
                        cellPU = new PdfPCell(new Phrase(""));
                        cellTotal = new PdfPCell(new Phrase(""));
                    }
                } else {
                    int quantite = produit.getQuantite() != null ? produit.getQuantite() : 1;
                    double prix = produit.getPrix();
                    double montant = quantite * prix;

                    cellQte = new PdfPCell(new Phrase(String.valueOf(quantite), calibri11));
                    cellPU = new PdfPCell(new Phrase(String.format("%.2f", prix), calibri11));
                    cellTotal = new PdfPCell(new Phrase(String.format("%.2f", montant), calibri11));

                    cellQte.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellPU.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellTotal.setHorizontalAlignment(Element.ALIGN_CENTER);

                    totalSuppl += montant;
                }

                int border = Rectangle.LEFT | Rectangle.RIGHT;
                if (isSupplement || (categorie.equals(derniereCategorieNormale) && produit.equals(produits.get(produits.size() - 1)))) {
                    border |= Rectangle.BOTTOM;
                }

                cellDesignation.setBorder(border);
                cellQte.setBorder(border);
                cellPU.setBorder(border);
                cellTotal.setBorder(border);

                cellDesignation.setBorderWidth(0.5f);
                cellQte.setBorderWidth(0.5f);
                cellPU.setBorderWidth(0.5f);
                cellTotal.setBorderWidth(0.5f);

                table.addCell(cellDesignation);
                table.addCell(cellQte);
                table.addCell(cellPU);
                table.addCell(cellTotal);

                if (!isSupplement) {
                    currentIndex++;
                }
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));

        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalSuppl;

        Paragraph totalParag = new Paragraph("Total prestation : " + String.format("%.2f", totalGeneral) + " DH",
                getCalibriFont(13, Font.BOLD));
        totalParag.setAlignment(Element.ALIGN_CENTER);
        document.add(totalParag);
        document.add(new Paragraph(" "));

        document.close();
        writer.close();

        return out.toByteArray();
    }
}
