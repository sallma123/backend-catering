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
                header.scaleToFit(PageSize.A4.getWidth(), 100); // pleine largeur
            }
            if (new File(FOOTER_IMAGE_PATH).exists()) {
                footer = Image.getInstance(FOOTER_IMAGE_PATH);
                footer.scaleToFit(PageSize.A4.getWidth(), 80); // pleine largeur
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContentUnder();
        try {
            if (header != null) {
                // ✅ COLLER EN HAUT sans marge
                float x = 0;
                float y = PageSize.A4.getHeight() - header.getScaledHeight();
                header.setAbsolutePosition(x, y);
                cb.addImage(header);
            }

            if (footer != null) {
                // ✅ COLLER EN BAS sans marge
                float x = 0;
                float y = 0;
                footer.setAbsolutePosition(x, y);
                cb.addImage(footer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
