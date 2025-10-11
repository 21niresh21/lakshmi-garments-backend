package com.lakshmigarments.utility;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class TicketUtils {

    public Document createDocument(){
        return new Document();
    }

    public Document createBasicTicketDocument(float width, float height, float marginLeft, float marginRight, float marginTop, float marginBottom, String author, String title){
        Document document = new Document();
        Rectangle pageSize = new Rectangle(width, height);
        document.setPageSize(pageSize);
        document.setMargins(marginLeft, marginRight, marginTop, marginBottom);
        setMetadata(document, author, title);
        return document;
    }

    private void setMetadata(Document document, String author, String title){
        document.addAuthor(author);
        document.addTitle(title);
        document.addCreationDate();
    }

    public PdfWriter getPdfWriterInstance(Document document, ByteArrayOutputStream byteArrayOutputStream) throws DocumentException {
        return PdfWriter.getInstance(document, byteArrayOutputStream);
    }

    public void addLogoImage(Document document, String logoPath, float fitWidth, float fitHeight) throws DocumentException, IOException {
        Image logo = Image.getInstance(logoPath);
        logo.scaleToFit(fitWidth, fitHeight);
        logo.setAlignment(Image.ALIGN_CENTER);
        document.add(logo);
    }

    public void addLogoText(Document document, String text, Font font) throws DocumentException {
        Paragraph logoText = new Paragraph(text, font);
        logoText.setAlignment(Element.ALIGN_CENTER);
        document.add(logoText);
    }

    public void addBodyText(Document document, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    public void addQRCode(Document document, String maskData, float width, float height) throws DocumentException {
        BarcodeQRCode qrCode = new BarcodeQRCode(maskData, (int)width, (int)height, prepareHints());
        Image qrImage = qrCode.getImage();
        qrImage.scaleToFit(width, height);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);
    }

    private Map<EncodeHintType,Object> prepareHints(){
        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        return hints;
    }

    public byte[] generateTicket(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        return byteArrayOutputStream.toByteArray();
    }

}
