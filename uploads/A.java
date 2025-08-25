// 🔹 Ajout de la section avances + signature dans la même ligne
if ("PARTICULIER".equalsIgnoreCase(commande.getTypeClient().name())) {
    List<Avance> avances = commande.getAvances();

    PdfPTable avancesSignatureTable = new PdfPTable(2);
    avancesSignatureTable.setWidthPercentage(100);
    avancesSignatureTable.setWidths(new float[]{3f, 1.5f});

    // ---- Colonne gauche : Avances ----
    PdfPCell avancesCell = new PdfPCell();
    avancesCell.setBorder(Rectangle.NO_BORDER);
    avancesCell.setPaddingTop(12f);

    com.lowagie.text.List listeAvances = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
    listeAvances.setSymbolIndent(0);
    listeAvances.setIndentationLeft(0);
    listeAvances.setAutoindent(false);
    listeAvances.setListSymbol("");

    if (avances == null || avances.isEmpty()) {
        // 1er avance
        Phrase p1 = new Phrase();
        p1.add(formatNumeroAvance(1, calibri12Bold));
        p1.add(new Chunk(" avance (50%) :", calibri12Bold));
        listeAvances.add(new ListItem(p1));

        // 2ème avance
        Phrase p2 = new Phrase();
        p2.add(formatNumeroAvance(2, calibri12Bold));
        p2.add(new Chunk(" avance (25%) :", calibri12Bold));
        listeAvances.add(new ListItem(p2));

        // Reste
        listeAvances.add(new ListItem("Reste :", calibri12Bold));
    } else {
        for (int i = 0; i < avances.size(); i++) {
            Avance avance = avances.get(i);
            Phrase p = new Phrase();
            p.add(formatNumeroAvance(i + 1, calibri12Bold));
            p.add(new Chunk(" avance : " + String.format("%.2f", avance.getMontant()) + " DH", calibri12Bold));
            listeAvances.add(new ListItem(p));
        }
        listeAvances.add(new ListItem("Reste : " + String.format("%.2f", commande.getResteAPayer()) + " DH", calibri12Bold));
    }

    avancesCell.addElement(listeAvances);

    // ---- Colonne droite : Signature ----
    PdfPCell signatureCell = new PdfPCell();
    signatureCell.setBorder(Rectangle.NO_BORDER);
    signatureCell.setPaddingTop(20f);
    signatureCell.setPaddingBottom(1f);
    signatureCell.setPaddingRight(10f);
    signatureCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

    try {
        Image signature = Image.getInstance("uploads/signature.jpg");
        signature.scaleAbsolute(100, 50);
        signature.setAlignment(Image.ALIGN_RIGHT);
        signatureCell.addElement(signature);
    } catch (Exception e) {
        e.printStackTrace();
    }

    avancesSignatureTable.addCell(avancesCell);
    avancesSignatureTable.addCell(signatureCell);

    document.add(avancesSignatureTable);

    // 🔹 Note en dessous
    Font calibri11Note = getCalibriFont(11, Font.NORMAL);
    calibri11Note.setColor(customColor);

    Paragraph note = new Paragraph(
            "Notes : Toute annulation ou changement de dates n'engendrent pas de restitution d'avance ; un avoir d'une durée d'un an est fourni au client.",
            calibri11Note
    );
    note.setSpacingBefore(5f);
    document.add(note);

} else if ("ENTREPRISE".equalsIgnoreCase(commande.getTypeClient().name())) {
    List<Avance> avances = commande.getAvances();

    PdfPTable avancesSignatureTable = new PdfPTable(2);
    avancesSignatureTable.setWidthPercentage(100);
    avancesSignatureTable.setWidths(new float[]{3f, 1.5f});

    // ---- Colonne gauche : Avance ----
    PdfPCell avancesCell = new PdfPCell();
    avancesCell.setBorder(Rectangle.NO_BORDER);
    avancesCell.setPaddingTop(12f);

    double totalAvances = (avances == null || avances.isEmpty())
            ? 0
            : avances.stream().mapToDouble(Avance::getMontant).sum();

    Paragraph p = new Paragraph("Avance : " + String.format("%.2f", totalAvances) + " DH", calibri12Bold);
    avancesCell.addElement(p);

    // ---- Colonne droite : Signature ----
    PdfPCell signatureCell = new PdfPCell();
    signatureCell.setBorder(Rectangle.NO_BORDER);
    signatureCell.setPaddingTop(20f);
    signatureCell.setPaddingBottom(1f);
    signatureCell.setPaddingRight(10f);
    signatureCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

    try {
        Image signature = Image.getInstance("uploads/signature.jpg");
        signature.scaleAbsolute(100, 50);
        signature.setAlignment(Image.ALIGN_RIGHT);
        signatureCell.addElement(signature);
    } catch (Exception e) {
        e.printStackTrace();
    }

    avancesSignatureTable.addCell(avancesCell);
    avancesSignatureTable.addCell(signatureCell);

    document.add(avancesSignatureTable);

    // 🔹 Note en dessous
    Font calibri11Note = getCalibriFont(11, Font.NORMAL);
    calibri11Note.setColor(customColor);

    Paragraph note = new Paragraph(
            "Notes : Toute annulation ou changement de dates n'engendrent pas de restitution d'avance ; un avoir d'une durée d'un an est fourni au client.",
            calibri11Note
    );
    note.setSpacingBefore(5f);
    document.add(note);
}
