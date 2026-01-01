package com.lakshmigarments.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jobwork_receipt_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobworkReceiptItem {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	private JobworkReceipt jobworkReceipt;
	
	@ManyToOne
	private Item item;
	
	private Long receivedQuantity;
	
	private Long damagedQuantity;
	
	private Long purchaseQuantity;
	
	private Double purchaseRate;
	
	private Double wagePerItem;
	
}
