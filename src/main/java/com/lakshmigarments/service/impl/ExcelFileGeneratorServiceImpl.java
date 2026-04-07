package com.lakshmigarments.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.lakshmigarments.model.MaterialInventoryLedger;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.service.ExcelFileGeneratorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelFileGeneratorServiceImpl implements ExcelFileGeneratorService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(ExcelFileGeneratorServiceImpl.class);
	private final MaterialLedgerRepository ledgerRepository;

	@Override
	public byte[] generateMaterialInventoryLedgers() {
		
		List<MaterialInventoryLedger> inventoryLedgers = ledgerRepository.findAll();
		
		// Use SXSSFWorkbook for streaming to avoid OOM
		SXSSFWorkbook workbook = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
        Sheet sheet = workbook.createSheet("Invoices");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Datetime");
        header.createCell(2).setCellValue("Reference Type");
        header.createCell(3).setCellValue("Movement Type");
        header.createCell(4).setCellValue("Direction");
        header.createCell(5).setCellValue("Category");
        header.createCell(6).setCellValue("Sub Category");
        header.createCell(7).setCellValue("Quantity");
        header.createCell(8).setCellValue("Executed By");
        
        int rowNum = 1;
        for (MaterialInventoryLedger ledger : inventoryLedgers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(ledger.getId());
            
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");
            String formattedDate = ledger.getCreatedAt().format(formatter);
            
            row.createCell(1).setCellValue(formattedDate);
            row.createCell(2).setCellValue(ledger.getReferenceType().toString());
            row.createCell(3).setCellValue(ledger.getMovementType().toString());
            row.createCell(4).setCellValue(ledger.getDirection().toString());
            row.createCell(5).setCellValue(ledger.getCategory().getName());
            row.createCell(6).setCellValue(ledger.getSubCategory().getName());
            row.createCell(7).setCellValue(ledger.getQuantity());
            row.createCell(8).setCellValue(ledger.getCreatedBy());
        }

        // Write to byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
			workbook.write(out);
			workbook.dispose(); // dispose of temporary files backing this workbook on disk
			workbook.close();
		} catch (IOException e) {
			LOGGER.error("Error writing to file");
			e.printStackTrace();
		}

        byte[] excelBytes = out.toByteArray();
        return excelBytes;
		
	}

}

