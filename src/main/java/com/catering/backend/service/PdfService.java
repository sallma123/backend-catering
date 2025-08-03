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

        Font calibri16 = getCalibriFont(16, Font.NORMAL);
        Font calibri11 = getCalibriFont(11, Font.NORMAL);
        Font calibri11Bold = getCalibriFont(11, Font.BOLD);
        Font calibri12 = getCalibriFont(12, Font.NORMAL);
        Font calibri12Bold = getCalibriFont(12, Font.BOLD);
        Font calibri12Noir = getCalibriFont(12, Font.NORMAL); // Police noire
        calibri12Noir.setColor(Color.BLACK);

        Color customColor = new Color(18, 63, 76);
        calibri11.setColor(customColor);
        calibri11Bold.setColor(customColor);
        calibri12.setColor(customColor);
        calibri12Bold.setColor(customColor);

        String dateFiche = commande.getDateFiche() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(commande.getDateFiche()))
                : "??/??/????";

        PdfPTable headerLine = new PdfPTable(3);
        headerLine.setWidthPercentage(100);
        headerLine.setWidths(new float[]{4f, 1f, 4f});
        headerLine.addCell(createCell("Fiche technique N° " + commande.getNumeroCommande(), calibri16, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        headerLine.addCell(createCell("", calibri16, Element.ALIGN_CENTER, Rectangle.NO_BORDER));
        headerLine.addCell(createCell("Rabat le : " + dateFiche, calibri16, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
        document.add(headerLine);
        Paragraph space = new Paragraph();
        space.setSpacingBefore(6f); // ou 10f, selon le rendu souhaité
        document.add(space);

        PdfPTable infoLine = new PdfPTable(4);
        infoLine.setWidthPercentage(100);
        infoLine.setWidths(new float[]{3f, 3.3f, 3.3f, 3f});
        infoLine.addCell(createCell("Client : " + commande.getNomClient(), calibri12Noir, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        infoLine.addCell(createCell("Nbre de " +
                (commande.getTypeClient().name().equals("ENTREPRISE") ? "personnes" : "tables") +
                " : " + commande.getNombreTables(), calibri12Noir, Element.ALIGN_CENTER, Rectangle.NO_BORDER));
        String dateCommande = commande.getDate() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(commande.getDate()))
                : "??/??/????";
        infoLine.addCell(createCell("Date : " + dateCommande, calibri12Noir, Element.ALIGN_CENTER, Rectangle.NO_BORDER));

        infoLine.addCell(createCell("Salle : " + commande.getSalle(), calibri12Noir, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
        document.add(infoLine);

        space.setSpacingBefore(13f); // ou 10f, selon le rendu souhaité
        document.add(space);


        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5f, 1f, 1.3f, 1.8f});
        Color beige = new Color(221, 217, 195);
        String[] headers = {"Désignation", "Quantité", "PU (DH)", "Total"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, calibri12));
            headerCell.setBackgroundColor(beige);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBorder(Rectangle.BOX);
            headerCell.setBorderWidth(0.5f);
            headerCell.setPaddingBottom(6f); // ← ajoute de l'espace en bas
            table.addCell(headerCell);
        }


        Map<String, List<ProduitCommande>> produitsParCategorie = new LinkedHashMap<>();
        for (ProduitCommande produit : produitsCoches) {
            produitsParCategorie.computeIfAbsent(produit.getCategorie(), k -> new java.util.ArrayList<>()).add(produit);
        }

        List<String> categoriesNormales = produitsParCategorie.keySet().stream()
                .filter(c -> !c.equalsIgnoreCase("Supplément"))
                .collect(Collectors.toList());
        String derniereCategorieNormale = categoriesNormales.isEmpty() ? null : categoriesNormales.get(categoriesNormales.size() - 1);

        String valeurQuantiteGlobale = String.valueOf(commande.getNombreTables());
        String valeurPU = String.format("%.2f", commande.getPrixParTable());
        String valeurTotal = String.format("%.2f", commande.getNombreTables() * commande.getPrixParTable());

        int totalNormaux = produitsParCategorie.entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase("Supplément"))
                .mapToInt(e -> e.getValue().size()).sum();
        int indexAffichage = totalNormaux / 2;
        int index = 0;

        // Sections normales (sans bordures horizontales)
        for (Map.Entry<String, List<ProduitCommande>> entry : produitsParCategorie.entrySet()) {
            String categorie = entry.getKey();
            if (categorie.equalsIgnoreCase("Supplément")) continue;

            table.addCell(sectionCell(categorie + " :", calibri12Bold));
            table.addCell(sectionCell("", calibri12Bold));
            table.addCell(sectionCell("", calibri12Bold));
            table.addCell(sectionCell("", calibri12Bold));

            List<ProduitCommande> produits = entry.getValue();
            for (ProduitCommande produit : produits) {
                boolean lastOfLast = categorie.equals(derniereCategorieNormale)
                        && produit.equals(produits.get(produits.size() - 1));

                Paragraph para = new Paragraph("‐   " + produit.getNom(), calibri11);
                para.setLeading(0, 0.9f); // interligne normal
                para.setFirstLineIndent(1f);     // pas d'indentation pour la première ligne
                para.setIndentationLeft(19f);    // indentation pour les lignes suivantes

                PdfPCell cellDesignation = new PdfPCell();
                cellDesignation.addElement(para);

                PdfPCell cellQte = new PdfPCell();
                PdfPCell cellPU = new PdfPCell();
                PdfPCell cellTotal = new PdfPCell();

                if (index == indexAffichage) {
                    cellQte.setPhrase(new Phrase(valeurQuantiteGlobale, calibri12Noir));
                    cellPU.setPhrase(new Phrase(valeurPU, calibri12Noir));
                    cellTotal.setPhrase(new Phrase(valeurTotal, calibri12Noir));
                    cellQte.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellPU.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cellTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
                }

                for (PdfPCell c : List.of(cellDesignation, cellQte, cellPU, cellTotal)) {
                    c.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
                    c.setBorderWidth(0.5f);
                }

                table.addCell(cellDesignation);
                table.addCell(cellQte);
                table.addCell(cellPU);
                table.addCell(cellTotal);
                index++;
            }
        }

        // Matériel et Service (bordure dessous)
        PdfPCell msCell1 = new PdfPCell(new Phrase("Matériel et Service", calibri12Bold));
        PdfPCell msCell2 = new PdfPCell();
        PdfPCell msCell3 = new PdfPCell();
        PdfPCell msCell4 = new PdfPCell();

        for (PdfPCell cell : List.of(msCell1, msCell2, msCell3, msCell4)) {
            cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            cell.setBorderWidth(0.5f);
        }

        table.addCell(msCell1);
        table.addCell(msCell2);
        table.addCell(msCell3);
        table.addCell(msCell4);


        // Produits de Supplément (avec bordure complète)
        produitsParCategorie.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("Supplément"))
                .flatMap(e -> e.getValue().stream())
                .forEach(produit -> {
                    PdfPCell cellDesignation = new PdfPCell(new Phrase(produit.getNom(), calibri12Bold));
                    PdfPCell cellQte = new PdfPCell(new Phrase(String.valueOf(produit.getQuantite()), calibri12Noir));
                    PdfPCell cellPU = new PdfPCell(new Phrase(String.format("%.2f", produit.getPrix()), calibri12Noir));
                    double montant = produit.getPrix() * (produit.getQuantite() != null ? produit.getQuantite() : 1);
                    PdfPCell cellTotal = new PdfPCell(new Phrase(String.format("%.2f", montant), calibri12Noir));

                    for (PdfPCell c : List.of(cellQte, cellPU, cellTotal)) {
                        c.setHorizontalAlignment(Element.ALIGN_CENTER);
                    }

                    for (PdfPCell c : List.of(cellDesignation, cellQte, cellPU, cellTotal)) {
                        c.setBorder(Rectangle.BOX);
                        c.setBorderWidth(0.5f);
                        c.setPaddingBottom(6f);
                    }

                    table.addCell(cellDesignation);
                    table.addCell(cellQte);
                    table.addCell(cellPU);
                    table.addCell(cellTotal);
                });

        document.add(table);
        document.add(new Paragraph(" "));

        double totalSuppl = produitsParCategorie.getOrDefault("Supplément", List.of()).stream()
                .mapToDouble(p -> p.getPrix() * (p.getQuantite() != null ? p.getQuantite() : 1)).sum();

        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalSuppl;
        Paragraph totalParag = new Paragraph("Total prestation : " + String.format("%.2f", totalGeneral) + " DH", getCalibriFont(13, Font.BOLD));
        totalParag.setAlignment(Element.ALIGN_CENTER);
        document.add(totalParag);
        document.close();
        writer.close();
        return out.toByteArray();
    }

    private PdfPCell createCell(String text, Font font, int align, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setBorder(border);
        return cell;
    }

    private PdfPCell sectionCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        cell.setBorderWidth(0.5f);
        return cell;
    }
}
