package com.lakshmigarments.model;

import jakarta.persistence.Column;
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
@Table(name = "bales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bale {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 100, nullable = false, unique = true)
	private String baleNumber;
	
	private Integer quantity;
	
	private Double length;
	
	private Double price;
	
	@Column(length = 200)
	private String quality;
	
	@ManyToOne
	private LorryReceipt lorryReceipt;
	
	@ManyToOne
	private SubCategory subCategory;
	
	public Bale(String baleNumber, Integer quantity, Double length, Double price, String quality, 
			 SubCategory subCategory, LorryReceipt lorryReceipt) {
		this.baleNumber = baleNumber;
		this.quantity = quantity;
		this.length = length;
		this.price = price;
		this.quality = quality;
		this.lorryReceipt = lorryReceipt;
		this.subCategory = subCategory;
	}
	
}
