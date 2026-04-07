package com.lakshmigarments.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.lakshmigarments.controller.JobworkController;
import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.service.PdfGenerator;
import com.lakshmigarments.utility.DateUtil;
import com.lakshmigarments.utility.TamilTextImageUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class JobWorkPdfGenerator implements PdfGenerator {
	
	@Override
	public byte[] generatePdf(JobworkRequestDTO jobworkRequestDTO) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			
			/* ================= PAGE SETUP ================= */

			float width = 226f; // 80mm
			float height = 300f;

			Rectangle pageSize = new Rectangle(width, height);
			Document document = new Document(pageSize, 10, 10, 10, 10);
			PdfWriter.getInstance(document, baos);
			document.open();

			/* ================= LOGOS ================= */

			InputStream leftLogoStream = getClass().getClassLoader()
					.getResourceAsStream("static/lg_twin_roses.jpeg");

			InputStream rightLogoStream = getClass().getClassLoader().getResourceAsStream("static/lg_lotus.jpeg");

			float logoSize = 28f;
			float topY = height - 45f;

			if (leftLogoStream != null) {
				Image leftLogo = Image.getInstance(leftLogoStream.readAllBytes());
				leftLogo.scaleToFit(logoSize, logoSize);
				leftLogo.setAbsolutePosition(10f, topY);
				document.add(leftLogo);
			}

			if (rightLogoStream != null) {
				Image rightLogo = Image.getInstance(rightLogoStream.readAllBytes());
				rightLogo.scaleToFit(logoSize, logoSize);
				rightLogo.setAbsolutePosition(width - logoSize - 10f, topY);
				document.add(rightLogo);
			}

			/* ================= FONTS ================= */

			Font titleFont = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font subTitleFont = new Font(Font.HELVETICA, 11, Font.BOLD);
			Font labelFont = new Font(Font.HELVETICA, 9, Font.BOLD);
			Font valueFont = new Font(Font.HELVETICA, 9);

			/* ================= TITLE ================= */

			Paragraph title = new Paragraph("Lakshmi Garments", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingBefore(10f);
			document.add(title);

			Paragraph subTitle = new Paragraph("Jobwork Slip\n", subTitleFont);
			subTitle.setAlignment(Element.ALIGN_CENTER);
			document.add(subTitle);

			/* ================= DETAILS TABLE ================= */

			PdfPTable detailsTable = new PdfPTable(2);
			detailsTable.setWidthPercentage(100);
			detailsTable.setWidths(new float[] { 1.2f, 1.8f });
			detailsTable.setSpacingBefore(5f);
			
			Font jobNoFont = new Font(Font.HELVETICA, 10, Font.BOLD);

			Paragraph jobNo = new Paragraph(
			        "JOB # " + jobworkRequestDTO.getJobworkNumber(),
			        jobNoFont
			);
			jobNo.setAlignment(Element.ALIGN_CENTER);
			jobNo.setSpacingAfter(6f);

			document.add(jobNo);


//			addRow(detailsTable, "\nJOB #" + jobworkRequestDTO.getJobworkNumber() + "\n\n", "", labelFont, valueFont);

			String formattedNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a"));
			addRow(detailsTable, "Assigned To", jobworkRequestDTO.getEmployeeName(), labelFont, valueFont);
//			addRow(detailsTable, "Jobwork No.", jobworkRequestDTO.getJobworkNumber(), labelFont, valueFont);
			addRow(detailsTable, "Assigned At", formattedNow, labelFont, valueFont);
			addRow(detailsTable, "Jobwork Type", jobworkRequestDTO.getJobworkType().toString(), labelFont,
					valueFont);

			document.add(detailsTable);

			document.add(new Paragraph("---------------------------------------------------"));

			if (jobworkRequestDTO.getJobworkType() == JobworkType.CUTTING) {
				addCuttingQuantity(document, jobworkRequestDTO, valueFont, labelFont);
				addRemarks(document, jobworkRequestDTO, width);
				
			} else {
				addItemQuantityTable(document, jobworkRequestDTO, labelFont, valueFont);
				addRemarks(document, jobworkRequestDTO, width);
			}
			document.close();
			return baos.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error generating PDF", e);
		}
	}
	
	private void addCuttingQuantity(Document document, JobworkRequestDTO jobworkRequestDTO, 
			Font valueFont, Font labelFont) throws Exception {
		PdfPTable itemTable = new PdfPTable(2);
		itemTable.setWidthPercentage(100);
		itemTable.setWidths(new float[] { 1.2f, 1.8f });
		itemTable.setSpacingBefore(5f);

		addRow(itemTable, "Quantity", jobworkRequestDTO.getQuantities().get(0) + " pcs", labelFont, valueFont);
		

		document.add(itemTable);
	}
	
	private void addRemarks(Document document, JobworkRequestDTO jobworkRequestDTO, 
			float width) {
		
		if (jobworkRequestDTO.getRemarks() == null || jobworkRequestDTO.
				getRemarks().isBlank()) {
	        return;
	    }
		
		PdfPTable remarkTable = new PdfPTable(1);
		remarkTable.setWidthPercentage(90);
		remarkTable.setHorizontalAlignment(Element.ALIGN_CENTER);
		remarkTable.setSpacingBefore(6f);
		remarkTable.setSpacingAfter(6f);
		remarkTable.setTotalWidth(width * 0.9f);
		remarkTable.setLockedWidth(true);

		Paragraph remarkPara = new Paragraph();
		remarkPara.setLeading(10f);              // control line height
		remarkPara.setIndentationLeft(0);        // remove paragraph indent
		remarkPara.setAlignment(Element.ALIGN_LEFT);

		Image tamilRemarkImage = TamilTextImageUtil.tamilTextToPdfImage(
		        jobworkRequestDTO.getRemarks(),
		        width * 0.92f
		);

		tamilRemarkImage.setAlignment(Image.ALIGN_CENTER);
		document.add(tamilRemarkImage);


		PdfPCell remarkCell = new PdfPCell(remarkPara);
		remarkCell.setBorder(Rectangle.NO_BORDER);
		remarkCell.setPaddingLeft(0);
		remarkCell.setPaddingRight(0);
		remarkCell.setPaddingTop(0);
		remarkCell.setPaddingBottom(0);
		remarkCell.setUseAscender(true);
		remarkCell.setUseDescender(true);
		remarkCell.setHorizontalAlignment(Element.ALIGN_LEFT);

		remarkTable.addCell(remarkCell);
		document.add(remarkTable);
	}
	
	private void addItemQuantityTable(
	        Document document,
	        JobworkRequestDTO dto,
	        Font labelFont,
	        Font valueFont) throws Exception {

	    PdfPTable table = new PdfPTable(2);
	    table.setWidthPercentage(100);
	    table.setWidths(new float[]{2.5f, 0.8f});
	    table.setSpacingBefore(6f);

	    // Header
	    PdfPCell itemHeader = new PdfPCell(new Paragraph("Item", labelFont));
	    itemHeader.setBorder(Rectangle.NO_BORDER);
	    itemHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
	    itemHeader.setPadding(4f);

	    PdfPCell qtyHeader = new PdfPCell(new Paragraph("Qty", labelFont));
	    qtyHeader.setBorder(Rectangle.NO_BORDER);
	    qtyHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
	    qtyHeader.setPadding(4f);

	    table.addCell(itemHeader);
	    table.addCell(qtyHeader);

	    int totalQty = 0;
	    // Loop rows
	    for (int i = 0; i < dto.getItemNames().size(); i++) {

	        PdfPCell itemCell = new PdfPCell(
	                new Paragraph(dto.getItemNames().get(i), valueFont)
	        );
	        itemCell.setBorder(Rectangle.NO_BORDER);
	        itemCell.setHorizontalAlignment(Element.ALIGN_LEFT);
	        itemCell.setPadding(3f);

	        PdfPCell qtyCell = new PdfPCell(
	                new Paragraph(dto.getQuantities().get(i) + "", valueFont)
	        );
	        qtyCell.setBorder(Rectangle.NO_BORDER);
	        qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	        qtyCell.setPadding(3f);

	        table.addCell(itemCell);
	        table.addCell(qtyCell);
	        totalQty += dto.getQuantities().get(i);
	    }
	    
	 // -------- Total Row (RIGHT aligned, no divider) --------

	 // Empty left cell
	 PdfPCell emptyCell = new PdfPCell(new Phrase(""));
	 emptyCell.setBorder(Rectangle.NO_BORDER);
	 table.addCell(emptyCell);

	 // Right cell with "Total 260"
	 PdfPCell totalCell = new PdfPCell(
	         new Phrase("Total " + totalQty, labelFont)
	 );
	 totalCell.setBorder(Rectangle.NO_BORDER);
	 totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	 totalCell.setPaddingTop(6f);
	 totalCell.setPaddingBottom(2f);

	 table.addCell(totalCell);


	    document.add(table);
	}


	/* ================= HELPER METHOD ================= */

	private static void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
		PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
		labelCell.setBorder(Rectangle.NO_BORDER);
		labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		labelCell.setPadding(2f);
		labelCell.setNoWrap(true);

		PdfPCell valueCell = new PdfPCell(new Paragraph(value, valueFont));
		valueCell.setBorder(Rectangle.NO_BORDER);
		valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		valueCell.setPadding(2f);

		table.addCell(labelCell);
		table.addCell(valueCell);
	}
}
