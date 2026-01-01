package com.lakshmigarments.dto;

import java.sql.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateStockDTO {
	
	@NotBlank(message = "Invoice number is mandatory")
	@Size(max = 100)
	private String invoiceNumber;
	
	@PastOrPresent
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	private Date invoiceDate;
	
	@PastOrPresent
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
	private Date receivedDate;
	
	@NotNull(message = "Supplier ID is mandatory")
	@Positive(message = "Supplier ID should be positive")
	private Long supplierID;
	
	@NotNull(message = "Transport ID is mandatory")
	@Positive(message = "Transport ID should be positive")
	private Long transportID;
	
	private Double transportCost;
	
	private Boolean isTransportPaid;
	
	private Long createdById;
	
	@CreationTimestamp
	private Long createdAt;
	
	@NotNull
	private List<CreateLorryReceiptDTO> lorryReceipts;
}
