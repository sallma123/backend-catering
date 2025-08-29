package com.catering.backend.service;

import com.catering.backend.model.Avance;
import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.repository.CommandeRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    // 🔹 Utilitaire pour écrire "1er" / "2ème" / "3ème" avec suffixe en exposant
    private Phrase formatNumeroAvance(int numero, Font baseFont) {
        Phrase phrase = new Phrase();
        // chiffre normal
        phrase.add(new Chunk(String.valueOf(numero), baseFont));
        // suffixe en exposant (plus petit)
        String suffixe = (numero == 1) ? "er" : "ème";
        Font small = new Font(baseFont);                  // copie pour conserver style/couleur
        small.setSize(Math.max(6, baseFont.getSize() - 3)); // un peu plus petit (min 6)
        Chunk sup = new Chunk(suffixe, small);
        sup.setTextRise(4f); // monte le texte (exposant)
        phrase.add(sup);
        return phrase;
    }

    public byte[] genererFicheCommande(Long commandeId) throws Exception {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new Exception("Commande introuvable"));
        boolean masquerPrix = "PARTENAIRE".equalsIgnoreCase(commande.getTypeClient().name());

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
                para.setLeading(0, 0.9f);    // interligne normal
                para.setFirstLineIndent(1f); // pas d'indentation pour la première ligne
                para.setIndentationLeft(19f);// indentation pour les lignes suivantes

                PdfPCell cellDesignation = new PdfPCell();
                cellDesignation.addElement(para);

                PdfPCell cellQte = new PdfPCell();
                PdfPCell cellPU = new PdfPCell();
                PdfPCell cellTotal = new PdfPCell();

                if (index == indexAffichage) {
                    cellQte.setPhrase(new Phrase(valeurQuantiteGlobale, calibri12Noir));
                    cellPU.setPhrase(new Phrase(masquerPrix ? "" : valeurPU, calibri12Noir));
                    cellTotal.setPhrase(new Phrase(masquerPrix ? "" : valeurTotal, calibri12Noir));
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
                    PdfPCell cellPU = new PdfPCell(new Phrase(masquerPrix ? "" : String.format("%.2f", produit.getPrix()), calibri12Noir));
                    double montant = produit.getPrix() * (produit.getQuantite() != null ? produit.getQuantite() : 1);
                    PdfPCell cellTotal = new PdfPCell(new Phrase(masquerPrix ? "" : String.format("%.2f", montant), calibri12Noir));

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

        document.add(new Paragraph(" "));

        double totalSuppl = produitsParCategorie.getOrDefault("Supplément", List.of()).stream()
                .mapToDouble(p -> p.getPrix() * (p.getQuantite() != null ? p.getQuantite() : 1)).sum();

        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalSuppl;

        // Ligne Total finale fusionnée (colspan=3)
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total", calibri12Bold));
        totalLabelCell.setColspan(3); // Fusionne Désignation + Quantité + PU
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalLabelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totalLabelCell.setPaddingBottom(5f);
        totalLabelCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
        totalLabelCell.setBorderWidth(0.5f);

        // Cellule valeur total
        PdfPCell totalValueCell = new PdfPCell(new Phrase(masquerPrix ? "" : String.format("%.2f", totalGeneral), calibri12Noir));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalValueCell.setPaddingBottom(5f);
        totalValueCell.setBorder(Rectangle.RIGHT | Rectangle.BOTTOM);
        totalValueCell.setBorderWidth(0.5f);

        // Ajout au tableau
        table.addCell(totalLabelCell); // Colspan 3 colonnes
        table.addCell(totalValueCell); // Dernière colonne

        document.add(table);

// 🔹 Avances + signature (même ligne)
        if ("PARTICULIER".equalsIgnoreCase(commande.getTypeClient().name())) {
            // === LOGIQUE PARTICULIER (inchangée) ===
            List<Avance> avances = commande.getAvances();

            PdfPTable avancesSignatureTable = new PdfPTable(2);
            avancesSignatureTable.setWidthPercentage(100);
            avancesSignatureTable.setWidths(new float[]{3f, 1.5f});

            PdfPCell avancesCell = new PdfPCell();
            avancesCell.setBorder(Rectangle.NO_BORDER);
            avancesCell.setPaddingTop(12f);

            com.lowagie.text.List listeAvances = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            listeAvances.setSymbolIndent(0);
            listeAvances.setIndentationLeft(0);
            listeAvances.setAutoindent(false);
            listeAvances.setListSymbol("");

            if (avances == null || avances.isEmpty()) {
                Phrase p1 = new Phrase();
                p1.add(formatNumeroAvance(1, calibri12Bold));
                p1.add(new Chunk(" avance (50%) :", calibri12Bold));
                ListItem li1 = new ListItem(p1);
                li1.setSpacingAfter(8f);
                listeAvances.add(li1);

                Phrase p2 = new Phrase();
                p2.add(formatNumeroAvance(2, calibri12Bold));
                p2.add(new Chunk(" avance (25%) :", calibri12Bold));
                ListItem li2 = new ListItem(p2);
                li2.setSpacingAfter(8f);
                listeAvances.add(li2);

                ListItem liR = new ListItem(new Phrase("Reste :", calibri12Bold));
                liR.setSpacingAfter(8f);
                listeAvances.add(liR);
            } else {
                for (int i = 0; i < avances.size(); i++) {
                    Avance avance = avances.get(i);
                    Phrase p = new Phrase();
                    p.add(formatNumeroAvance(i + 1, calibri12Bold));
                    p.add(new Chunk(" avance : " +
                                String.format("%.2f", avance.getMontant()) +
                                " DH " + "(Le " + avance.getDate() + ", via " + avance.getType() +" )", calibri12Bold));

                    ListItem li = new ListItem(p);
                    li.setSpacingAfter(8f);
                    listeAvances.add(li);
                }
                ListItem liReste = new ListItem(new Phrase(
                        "Reste : " + String.format("%.2f", commande.getResteAPayer()) + " DH", calibri12Bold));
                liReste.setSpacingAfter(8f);
                listeAvances.add(liReste);
            }

            avancesCell.addElement(listeAvances);

            PdfPCell signatureCell = new PdfPCell();
            signatureCell.setBorder(Rectangle.NO_BORDER);
            signatureCell.setPaddingTop(20f);
            signatureCell.setPaddingBottom(1f);
            signatureCell.setPaddingRight(10f);
            signatureCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            boolean afficherSignatureCachet = commande.isAfficherSignatureCachet()
                    && ("PARTICULIER".equalsIgnoreCase(commande.getTypeClient().name())
                    || "ENTREPRISE".equalsIgnoreCase(commande.getTypeClient().name()));

            if (afficherSignatureCachet) {
                try {
                    InputStream is = getClass().getResourceAsStream("/upload/signature.jpg");
                    if (is != null) {
                        Image signature = Image.getInstance(is.readAllBytes());
                        signature.scaleAbsolute(120, 60);
                        signature.setAlignment(Image.ALIGN_RIGHT);
                        signatureCell.addElement(signature);
                    } else {
                        System.err.println("⚠️ Signature non trouvée dans resources/uploads !");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            avancesSignatureTable.addCell(avancesCell);
            avancesSignatureTable.addCell(signatureCell);

            document.add(avancesSignatureTable);

            Font calibri11Note = getCalibriFont(11, Font.NORMAL);
            calibri11Note.setColor(customColor);

            Paragraph note = new Paragraph(
                    "Notes : Toute annulation ou changement de dates n'engendrent pas de restitution d'avance ; un avoir d'une durée d'un an est fourni au client.",
                    calibri11Note
            );
            note.setSpacingBefore(5f);
            document.add(note);

        } else if ("ENTREPRISE".equalsIgnoreCase(commande.getTypeClient().name())) {
            // === LOGIQUE ENTREPRISE (nouvelle) ===
            List<Avance> avances = commande.getAvances();
            double totalAvances = (avances == null) ? 0 : avances.stream().mapToDouble(Avance::getMontant).sum();

            PdfPTable avancesSignatureTable = new PdfPTable(2);
            avancesSignatureTable.setWidthPercentage(100);
            avancesSignatureTable.setWidths(new float[]{3f, 1.5f});

            PdfPCell avancesCell = new PdfPCell();
            avancesCell.setBorder(Rectangle.NO_BORDER);
            avancesCell.setPaddingTop(12f);

            com.lowagie.text.List listeAvances = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            listeAvances.setSymbolIndent(0);
            listeAvances.setIndentationLeft(0);
            listeAvances.setAutoindent(false);
            listeAvances.setListSymbol("");

            // Avance (total ou 0)
            ListItem liAvance = new ListItem(new Phrase(
                    "Avance : " + String.format("%.2f", totalAvances) + " DH", calibri12Bold));
            liAvance.setSpacingAfter(8f);
            listeAvances.add(liAvance);

            // Reste
            ListItem liReste = new ListItem(new Phrase(
                    "Reste : " + String.format("%.2f", commande.getResteAPayer()) + " DH", calibri12Bold));
            liReste.setSpacingAfter(8f);
            listeAvances.add(liReste);

            avancesCell.addElement(listeAvances);

            PdfPCell signatureCell = new PdfPCell();
            signatureCell.setBorder(Rectangle.NO_BORDER);
            signatureCell.setPaddingTop(15f);
            signatureCell.setPaddingBottom(0f);
            signatureCell.setPaddingRight(10f);
            signatureCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                Image signature = Image.getInstance("uploads/signature.jpg");
                signature.scaleAbsolute(120, 60);
                signature.setAlignment(Image.ALIGN_RIGHT);
                signatureCell.addElement(signature);
            } catch (Exception e) {
                e.printStackTrace();
            }

            avancesSignatureTable.addCell(avancesCell);
            avancesSignatureTable.addCell(signatureCell);

            document.add(avancesSignatureTable);

// === Ajout des conditions spécifiques ENTREPRISE ===
            Font calibri11Note = getCalibriFont(11, Font.NORMAL);
            calibri11Note.setColor(customColor);

            Paragraph conditions = new Paragraph(
                    "Conditions de règlement : 50% à la commande et 50% à la livraison",
                    calibri11Note
            );
            conditions.setSpacingBefore(5f);
            document.add(conditions);

            Paragraph validite = new Paragraph(
                    "Cette offre est valable 1 mois à partir de la date du devis communiqué.",
                    calibri11Note
            );
            validite.setSpacingBefore(3f);
            document.add(validite);

        }


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
