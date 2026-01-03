package com.lakshmigarments.dto;

import java.sql.Date;
import java.util.List;

import com.lakshmigarments.model.LorryReceipt;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompleteInvoiceDTO {

private Long id;
	
	private boolean canEdit;

	private String invoiceNumber;
	
	private Date invoiceDate;
	
	private Date receivedDate;
	
	private String supplierName;
	
	private String transportName;
	
	private Boolean isTransportPaid;
	
	private Double transportCost;
	
	private Integer noOfBales;
	
	private Integer noOfLorryReceipts;
	
	private Double value;
	
	private Long totalQuantity;
	
	private List<String> categories;
	
	private List<String> subCategories;
	
	private List<String> qualities;
	
	private List<Double> lengths;
	
	private List<LorryReceiptDTO> lorryReceiptDTOs;
	
}
