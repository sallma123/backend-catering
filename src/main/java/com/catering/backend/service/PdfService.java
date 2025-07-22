package com.catering.backend.service;

import com.catering.backend.model.Commande;
import com.catering.backend.model.ProduitCommande;
import com.catering.backend.repository.CommandeRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

        // 📄 Créer le document avec marges pour header/footer
        Document document = new Document(PageSize.A4, 36, 36, 120, 100);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        // 🎯 Ajouter entête/pied de page
        writer.setPageEvent(new HeaderFooterEvent());

        document.open();

        // ✅ Titre
        Paragraph title = new Paragraph("Fiche technique", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // ✅ Infos de la commande
        document.add(new Paragraph("Client : " + commande.getNomClient()));
        document.add(new Paragraph("Date : " + commande.getDate()));
        document.add(new Paragraph("Salle : " + commande.getSalle()));
        document.add(new Paragraph("Nombre de tables : " + commande.getNombreTables()));
        document.add(new Paragraph(" "));

        // ✅ Regrouper par catégorie
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

            for (ProduitCommande produit : produitsParCategorie.get(categorie)) {
                table.addCell(produit.getNom());
                table.addCell("1");
                table.addCell(String.valueOf(produit.getPrix()));
                table.addCell(String.valueOf(produit.getPrix()));
            }

            document.add(table);
            document.add(new Paragraph(" "));
        }

        // ✅ Total général
        double totalProduits = produitsCoches.stream()
                .mapToDouble(ProduitCommande::getPrix)
                .sum();
        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalProduits;

        document.add(new Paragraph(" "));
        Paragraph totalParag = new Paragraph("Total général : " + totalGeneral + " DH",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        totalParag.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalParag);

        document.close();
        writer.close();

        return out.toByteArray();
    }
}
