package com.lakshmigarments.utility;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import com.lowagie.text.Image;

public class TamilTextImageUtil {

public static Image tamilTextToPdfImage(String text, float pdfWidthPt) {
    try {
        final int DPI = 240;  // ⭐ key change (try 200–300)

        // Convert PDF points → pixels
        float scale = DPI / 72f;
        int imgWidthPx = Math.round(pdfWidthPt * scale);

        // Load font
        InputStream fontStream = TamilTextImageUtil.class
                .getClassLoader()
                .getResourceAsStream("fonts/NotoSansTamil-Regular.ttf");

        Font tamilFont = Font.createFont(Font.TRUETYPE_FONT, fontStream)
                .deriveFont(10f * scale); // scale font size too

        // Measure
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(tamilFont);
        FontMetrics fm = g.getFontMetrics();

        List<String> lines = wrapText(text, fm, imgWidthPx - 20);
        int lineHeight = fm.getHeight();
        int imgHeightPx = lines.size() * lineHeight + 10;
        g.dispose();

        // High-resolution image
        BufferedImage image = new BufferedImage(
                imgWidthPx,
                imgHeightPx,
                BufferedImage.TYPE_INT_RGB
        );

        g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgWidthPx, imgHeightPx);

        // ⭐ Rendering quality
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        g.setColor(Color.BLACK);
        g.setFont(tamilFont);

        int y = lineHeight;
        for (String line : lines) {
            g.drawString(line, 10, y);
            y += lineHeight;
        }
        g.dispose();

        Image pdfImage = Image.getInstance(image, null);

        // ⭐ Scale DOWN to PDF width (never up)
        pdfImage.scaleToFit(pdfWidthPt, pdfWidthPt * 5);
        pdfImage.setAlignment(Image.ALIGN_CENTER);

        return pdfImage;

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}


private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
    List<String> lines = new ArrayList<>();

    // 1️⃣ Split by newline first
    for (String paragraph : text.split("\\r?\\n")) {

        // Preserve empty lines
        if (paragraph.trim().isEmpty()) {
            lines.add("");
            continue;
        }

        StringBuilder line = new StringBuilder();

        // 2️⃣ Wrap words inside each paragraph
        for (String word : paragraph.split(" ")) {
            if (fm.stringWidth(line + (line.length() == 0 ? "" : " ") + word) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }
    }

    return lines;
}

}

