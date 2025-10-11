package com.lakshmigarments.utility;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Getter
@Component
@RequiredArgsConstructor
public class TicketHelper {

    private static final float marginLeft = 5;
    private static final float marginRight = 5;
    private static final float marginTop = 5;
    private static final float marginBottom = 5;
    private static final float width = 58f / 25.4f * 72f;
    private static final float height = 100f / 25.4f * 72f;
    private static final float logoWidth = 50;
    private static final float logoHeight = 50;
    private static final float qrWidth = 80;
    private static final float qrHeight = 80;
    private static final String AUTHOR = "Lakshmi Garments";
    private static final String LOGO_PATH = "src/main/resources/logo.jpg";
    private static final Font FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font LOGO_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private final TicketUtils ticketUtils;

    private Document document = null;
    private PdfWriter pdfWriter = null;
    private ByteArrayOutputStream byteArrayOutputStream = null;

    public void prepareDocument(String title) throws DocumentException, IOException {
        document = ticketUtils.createBasicTicketDocument(width,height,marginLeft,marginRight,marginTop,marginBottom,AUTHOR,title);
        preparePdfWriter(document);
    }

    private void preparePdfWriter(Document document) throws DocumentException {
        if(pdfWriter==null || byteArrayOutputStream==null){
            byteArrayOutputStream = new ByteArrayOutputStream();
            pdfWriter = ticketUtils.getPdfWriterInstance(document, byteArrayOutputStream);
        }
        pdfWriter.open();
        document.open();
    }

    public void addContent(Document document, String bodyContent, String title) throws DocumentException, IOException {
        //ticketUtils.addLogoImage(document,LOGO_PATH,logoWidth,logoHeight);
        ticketUtils.addLogoText(document,AUTHOR,LOGO_FONT);
        ticketUtils.addBodyText(document,bodyContent,FONT);
        ticketUtils.addQRCode(document,title,qrWidth,qrHeight);
    }

    public byte[] generateTicket() throws IOException {
        document.close();
        pdfWriter.close();
        byte[] ticket = ticketUtils.generateTicket(byteArrayOutputStream);
        byteArrayOutputStream.close();
        return ticket;
    }
}
