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

    public byte[] genererFicheCommande(Long commandeId) throws Exception {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new Exception("Commande introuvable"));

        List<ProduitCommande> produitsCoches = commande.getProduits().stream()
                .filter(ProduitCommande::isSelectionne)
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 120, 100);
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

        Font normal16 = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.NORMAL);

// Cellule à gauche : Fiche technique
        PdfPCell leftCell = new PdfPCell(new Phrase("Fiche technique N° " + commande.getNumeroCommande(), normal16));
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        leftCell.setBorder(Rectangle.NO_BORDER);

// Cellule vide pour espacement
        PdfPCell spaceCell = new PdfPCell(new Phrase(""));
        spaceCell.setBorder(Rectangle.NO_BORDER);

// Cellule à droite : Rabat le
        PdfPCell rightCell = new PdfPCell(new Phrase("Rabat le : " + dateFiche, normal16));
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setBorder(Rectangle.NO_BORDER);

        headerLine.addCell(leftCell);
        headerLine.addCell(spaceCell);
        headerLine.addCell(rightCell);

        document.add(headerLine);
        document.add(new Paragraph(" "));


        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new int[]{1, 1});

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph("Client : " + commande.getNomClient()));
        left.addElement(new Paragraph("Nbre de " + (commande.getTypeClient().name().equals("ENTREPRISE") ? "personnes" : "tables") + " : " + commande.getNombreTables()));

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.addElement(new Paragraph("Date : " + commande.getDate()));
        right.addElement(new Paragraph("Salle : " + commande.getSalle()));

        infoTable.addCell(left);
        infoTable.addCell(right);
        document.add(infoTable);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 2, 2});

        String[] headers = {"Désignation", "Quantité", "PU (DH)", "Total"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            table.addCell(headerCell);
        }

        Map<String, List<ProduitCommande>> produitsParCategorie = produitsCoches.stream()
                .collect(Collectors.groupingBy(ProduitCommande::getCategorie));

        double totalSuppl = 0;

        for (String categorie : produitsParCategorie.keySet()) {
            List<ProduitCommande> produits = produitsParCategorie.get(categorie);

            // Ligne section AVEC 4 cellules pour respecter la structure
            PdfPCell sectionDesignation = new PdfPCell(new Phrase(categorie + " :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            PdfPCell sectionQte = new PdfPCell(new Phrase(""));
            PdfPCell sectionPU = new PdfPCell(new Phrase(""));
            PdfPCell sectionTotal = new PdfPCell(new Phrase(""));

            table.addCell(sectionDesignation);
            table.addCell(sectionQte);
            table.addCell(sectionPU);
            table.addCell(sectionTotal);

            for (ProduitCommande produit : produits) {
                PdfPCell cellDesignation = new PdfPCell(new Phrase(" ‐ " + produit.getNom()));
                PdfPCell cellQte = new PdfPCell(new Phrase(""));
                PdfPCell cellPU = new PdfPCell(new Phrase(""));
                PdfPCell cellTotal = new PdfPCell(new Phrase(""));

                if (categorie.equalsIgnoreCase("Supplément") ||
                        List.of("Matériel et Service", "Prestataires", "Pièce montée").contains(categorie)) {
                    int quantite = produit.getQuantite() != null ? produit.getQuantite() : 1;
                    double prix = produit.getPrix();
                    double montant = quantite * prix;

                    cellQte.setPhrase(new Phrase(String.valueOf(quantite)));
                    cellPU.setPhrase(new Phrase(String.format("%.2f", prix)));
                    cellTotal.setPhrase(new Phrase(String.format("%.2f", montant)));

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

        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalSuppl;

        Paragraph totalParag = new Paragraph("Total prestation : " + String.format("%.2f", totalGeneral) + " DH",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
        totalParag.setAlignment(Element.ALIGN_CENTER);
        document.add(totalParag);
        document.add(new Paragraph(" "));

        Paragraph sign = new Paragraph("BELMOKHTAR karting SARL ‐ Services traiteur\n" +
                "Site web : hs-traiteur.com, contact : contact@hs-traiteur.com, Tél : 0661966939",
                FontFactory.getFont(FontFactory.HELVETICA, 9));
        sign.setAlignment(Element.ALIGN_CENTER);
        document.add(sign);

        document.close();
        writer.close();

        return out.toByteArray();
    }
}
