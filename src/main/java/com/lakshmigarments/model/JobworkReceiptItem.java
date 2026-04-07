package com.lakshmigarments.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
	
	// usable items returned
	private Long acceptedQuantity;
	
	private Long damagedQuantity;
	
	// sales done to the employee
	private Long salesQuantity;
	
	private Double salesPrice;
	
	// wage payable per item
	private Double wagePerItem;
	
	@OneToMany(mappedBy = "jobworkReceiptItem", cascade = CascadeType.PERSIST)
    private List<Damage> damages = new ArrayList<>();
	
}
