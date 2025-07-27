package com.catering.backend.service;

import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.repository.CommandeRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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

        Paragraph title = new Paragraph(
                "Fiche technique N° " + commande.getNumeroCommande(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)
        );
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

// ✅ Vérifie si c'est un client entreprise
        if ("ENTREPRISE".equalsIgnoreCase(commande.getTypeClient().name())) {
            String objet = commande.getObjet();
            if (objet == null || objet.trim().isEmpty()) {
                objet = commande.getTypeCommande().name(); // On utilise .name() car c’est un enum
            }
            Paragraph objetPara = new Paragraph("Objet : " + objet, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            objetPara.setSpacingAfter(10);
            document.add(objetPara);
        }


        document.add(new Paragraph("Client : " + commande.getNomClient()));
        document.add(new Paragraph("Date : " + commande.getDate()));
        document.add(new Paragraph("Salle : " + commande.getSalle()));
        document.add(new Paragraph("Nombre de tables : " + commande.getNombreTables()));
        document.add(new Paragraph(" "));

        Map<String, List<ProduitCommande>> produitsParCategorie = produitsCoches.stream()
                .collect(Collectors.groupingBy(ProduitCommande::getCategorie));

        for (String categorie : produitsParCategorie.keySet()) {
            document.add(new Paragraph(categorie, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Désignation");
            table.addCell("Quantité");
            table.addCell("PU (DH)");
            table.addCell("Total");

            List<ProduitCommande> produits = produitsParCategorie.get(categorie);

            if (!categorie.equalsIgnoreCase("Supplément")) {
                // ✅ Produits standards : 1 ligne par produit avec quantité = nombreTables
                for (ProduitCommande produit : produits) {
                    table.addCell(produit.getNom());
                    table.addCell(String.valueOf(commande.getNombreTables()));
                    table.addCell("");
                    table.addCell("");
                }
            } else {
                // ✅ Suppléments : chaque ligne a sa propre quantité et PU
                for (ProduitCommande produit : produits) {
                    int qte = produit.getQuantite() != null ? produit.getQuantite() : 1;
                    double total = produit.getPrix() * qte;

                    table.addCell(produit.getNom());
                    table.addCell(String.valueOf(qte));
                    table.addCell(String.format("%.2f", produit.getPrix()));
                    table.addCell(String.format("%.2f", total));
                }
            }

            document.add(table);
            document.add(new Paragraph(" "));
        }

        // ✅ Total général
        double totalSuppl = produitsCoches.stream()
                .filter(p -> p.getCategorie().equalsIgnoreCase("Supplément"))
                .mapToDouble(p -> p.getPrix() * (p.getQuantite() != null ? p.getQuantite() : 1))
                .sum();

        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalSuppl;

        document.add(new Paragraph(" "));
        Paragraph totalParag = new Paragraph("Total général : " + String.format("%.2f", totalGeneral) + " DH",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        totalParag.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalParag);

        document.close();
        writer.close();

        return out.toByteArray();
    }
}
