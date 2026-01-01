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
@Table(name = "batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(length = 100, nullable = false, unique = true)
	private String serialCode;
	
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	private Long availableQuantity;
	
	@ManyToOne
	private User createdBy;
	
	@ManyToOne
	private Category category;
	
	private String remarks;
	
	private Boolean isUrgent;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "batch_status", nullable = false)
    private BatchStatus batchStatus;

}
