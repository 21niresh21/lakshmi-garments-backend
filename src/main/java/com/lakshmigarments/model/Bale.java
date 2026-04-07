package com.lakshmigarments.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bales", uniqueConstraints = {
	    @UniqueConstraint(columnNames = {"baleNumber", "lorry_receipt_id"})
	})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bale extends BaseAuditable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 100, nullable = false)
	private String baleNumber;
	
	private Long quantity;
	
	private Double length;
	
	private Double price;
	
	@Column(length = 200)
	private String quality;
	
	@ManyToOne
	@JoinColumn(name = "lorry_receipt_id")
	private LorryReceipt lorryReceipt;
	
	@ManyToOne
	private Category category;
	
	@ManyToOne
	private SubCategory subCategory;
	
	public Bale(String baleNumber, Long quantity, Double length, Double price, String quality,
			 	SubCategory subCategory, Category category, LorryReceipt lorryReceipt) {
		this.baleNumber = baleNumber;
		this.quantity = quantity;
		this.length = length;
		this.price = price;
		this.quality = quality;
		this.lorryReceipt = lorryReceipt;
		this.subCategory = subCategory;
		this.category = category;
	}
	
}
