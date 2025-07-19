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
import java.util.stream.Collectors;

@Service
public class PdfService {

    @Autowired
    private CommandeRepository commandeRepository;

    private static final String HEADER_IMAGE_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "header.jpg";
    private static final String FOOTER_IMAGE_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "footer.jpg";

    public byte[] genererFicheCommande(Long commandeId) throws Exception {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new Exception("Commande introuvable"));

        List<ProduitCommande> produitsCoches = commande.getProduits().stream()
                .filter(ProduitCommande::isSelectionne)
                .collect(Collectors.toList());

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // ✅ Ajouter l'en-tête (image)
        File headerFile = new File(HEADER_IMAGE_PATH);
        if (headerFile.exists()) {
            Image headerImage = Image.getInstance(HEADER_IMAGE_PATH);
            headerImage.scaleToFit(500, 100);
            headerImage.setAlignment(Image.ALIGN_CENTER);
            document.add(headerImage);
        }

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

        // ✅ Tableau des produits
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell("Désignation");
        table.addCell("Quantité");
        table.addCell("PU (DH)");
        table.addCell("Total");

        for (ProduitCommande produit : produitsCoches) {
            table.addCell(produit.getNom());
            table.addCell("1"); // quantité par défaut
            table.addCell(String.valueOf(produit.getPrix()));
            table.addCell(String.valueOf(produit.getPrix()));
        }

        document.add(table);

        // ✅ Total général
        double totalProduits = produitsCoches.stream()
                .mapToDouble(ProduitCommande::getPrix)
                .sum();
        double totalGeneral = commande.getPrixParTable() * commande.getNombreTables() + totalProduits;

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total général : " + totalGeneral + " DH"));

        // ✅ Pied de page (image)
        File footerFile = new File(FOOTER_IMAGE_PATH);
        if (footerFile.exists()) {
            document.add(new Paragraph(" "));
            Image footerImage = Image.getInstance(FOOTER_IMAGE_PATH);
            footerImage.scaleToFit(500, 80);
            footerImage.setAlignment(Image.ALIGN_CENTER);
            document.add(footerImage);
        }

        document.close();
        writer.close();

        return out.toByteArray();
    }
}
