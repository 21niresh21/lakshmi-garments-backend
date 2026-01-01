package com.lakshmigarments.model;

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
@Table(name = "jobwork_return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobworkReturnItem {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	private JobworkReturn jobworkReturn;
	
	@ManyToOne
	private Item item;
	
	private Long quantityGood;
	
	private Long quantityDamaged;
	
}
