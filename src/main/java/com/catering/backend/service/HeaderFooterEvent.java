package com.catering.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.File;

public class HeaderFooterEvent extends PdfPageEventHelper {

    private static final String HEADER_IMAGE_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "header.jpg";
    private static final String FOOTER_IMAGE_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "footer.jpg";

    private Image header;
    private Image footer;

    public HeaderFooterEvent() {
        try {
            if (new File(HEADER_IMAGE_PATH).exists()) {
                header = Image.getInstance(HEADER_IMAGE_PATH);
                header.scaleToFit(500, 100);
            }
            if (new File(FOOTER_IMAGE_PATH).exists()) {
                footer = Image.getInstance(FOOTER_IMAGE_PATH);
                footer.scaleToFit(500, 80);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        try {
            if (header != null) {
                header.setAbsolutePosition(
                        (document.right() - document.left() - header.getScaledWidth()) / 2 + document.leftMargin(),
                        document.top() + 50
                );
                cb.addImage(header);
            }
            if (footer != null) {
                footer.setAbsolutePosition(
                        (document.right() - document.left() - footer.getScaledWidth()) / 2 + document.leftMargin(),
                        document.bottom() - 60
                );
                cb.addImage(footer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
