package com.lakshmigarments.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "material_inventory_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialInventoryLedger extends BaseAuditable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	private Category category;
	
	@ManyToOne
	private SubCategory subCategory;
	
	private Long quantity;
	
	private String unit;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private LedgerDirection direction;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 100)
    private MovementType movementType;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "reference_type", nullable = false, length = 100)
	private ReferenceType referenceType;
	
	private Long reference_id;
	
	private String remarks;

}
