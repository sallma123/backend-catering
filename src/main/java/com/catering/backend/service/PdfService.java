package com.catering.backend.service;

import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.repository.CommandeRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
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

        // ➤ Ligne avec Fiche technique N°... à gauche et Rabat le : ... à droite
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

// ➤ Ligne avec toutes les infos sur une seule ligne
        Font calibri12 = getCalibriFont(12, Font.NORMAL);
        Font calibri11 = getCalibriFont(11, Font.NORMAL);
        Font calibri11Bold = getCalibriFont(11, Font.BOLD);

        PdfPTable infoLine = new PdfPTable(4);
        infoLine.setWidthPercentage(100);
        infoLine.setWidths(new float[]{2.5f, 2.5f, 2.5f, 2.5f}); // 4 colonnes égales

// Colonnes
        PdfPCell cellClient = new PdfPCell(new Phrase("Client : " + commande.getNomClient(), calibri12));
        PdfPCell cellNbr = new PdfPCell(new Phrase("Nbre de " +
                (commande.getTypeClient().name().equals("ENTREPRISE") ? "personnes" : "tables") +
                " : " + commande.getNombreTables(), calibri12));
        PdfPCell cellDate = new PdfPCell(new Phrase("Date : " + commande.getDate(), calibri12));
        PdfPCell cellSalle = new PdfPCell(new Phrase("Salle : " + commande.getSalle(), calibri12));

// Alignement
        cellClient.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNbr.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellDate.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellSalle.setHorizontalAlignment(Element.ALIGN_RIGHT);

// Supprimer les bordures
        for (PdfPCell cell : List.of(cellClient, cellNbr, cellDate, cellSalle)) {
            cell.setBorder(Rectangle.NO_BORDER);
        }

// Ajout au tableau
        infoLine.addCell(cellClient);
        infoLine.addCell(cellNbr);
        infoLine.addCell(cellDate);
        infoLine.addCell(cellSalle);

        document.add(infoLine);


        document.add(new Paragraph(" "));


        // ➤ Tableau principal
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 2, 2});

        String[] headers = {"Désignation", "Quantité", "PU (DH)", "Total"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, calibri11Bold));
            table.addCell(headerCell);
        }

        Map<String, List<ProduitCommande>> produitsParCategorie = produitsCoches.stream()
                .collect(Collectors.groupingBy(ProduitCommande::getCategorie));

        double totalSuppl = 0;

        for (String categorie : produitsParCategorie.keySet()) {
            List<ProduitCommande> produits = produitsParCategorie.get(categorie);

            // ➤ Ligne de titre de section
            PdfPCell sectionDesignation = new PdfPCell(new Phrase(categorie + " :", getCalibriFont(12, Font.BOLD)));
            PdfPCell sectionQte = new PdfPCell(new Phrase(""));
            PdfPCell sectionPU = new PdfPCell(new Phrase(""));
            PdfPCell sectionTotal = new PdfPCell(new Phrase(""));

            table.addCell(sectionDesignation);
            table.addCell(sectionQte);
            table.addCell(sectionPU);
            table.addCell(sectionTotal);

            for (ProduitCommande produit : produits) {
                PdfPCell cellDesignation = new PdfPCell(new Phrase(" ‐ " + produit.getNom(), calibri11));
                PdfPCell cellQte = new PdfPCell(new Phrase("", calibri11));
                PdfPCell cellPU = new PdfPCell(new Phrase("", calibri11));
                PdfPCell cellTotal = new PdfPCell(new Phrase("", calibri11));

                if (categorie.equalsIgnoreCase("Supplément") ||
                        List.of("Matériel et Service", "Prestataires", "Pièce montée").contains(categorie)) {
                    int quantite = produit.getQuantite() != null ? produit.getQuantite() : 1;
                    double prix = produit.getPrix();
                    double montant = quantite * prix;

                    cellQte.setPhrase(new Phrase(String.valueOf(quantite), calibri11));
                    cellPU.setPhrase(new Phrase(String.format("%.2f", prix), calibri11));
                    cellTotal.setPhrase(new Phrase(String.format("%.2f", montant), calibri11));

                    totalSuppl += montant;
                }

                table.addCell(cellDesignation);
                table.addCell(cellQte);
                table.addCell(cellPU);
                table.addCell(cellTotal);
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));

        // ➤ Total
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
