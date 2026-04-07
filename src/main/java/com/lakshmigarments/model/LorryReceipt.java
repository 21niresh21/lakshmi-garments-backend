package com.lakshmigarments.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lorry_receipts")
@Data
@NoArgsConstructor
public class LorryReceipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 100, nullable = false)
	private String LrNumber;
	
	@ManyToOne
	private Invoice invoice;
	
	@OneToMany(mappedBy = "lorryReceipt")
	private List<Bale> bales;
	
}
