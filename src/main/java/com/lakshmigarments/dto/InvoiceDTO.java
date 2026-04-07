package com.lakshmigarments.dto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InvoiceDTO {

	private Long id;
	
	private String invoiceNumber;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate invoiceDate;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate receivedDate;
	
	private String supplierName;
	
	private String transportName;
	
	private Boolean isTransportPaid;
	
	private Double transportCost;
	
	private String createdBy;
	
	private LocalDateTime createdAt; 

}
