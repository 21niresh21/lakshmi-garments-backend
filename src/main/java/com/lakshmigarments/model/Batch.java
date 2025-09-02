package com.lakshmigarments.model;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batch")
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
	private Timestamp createdAt;
	
	@ManyToOne
	private Category category;
	
	private String remarks;
	
	private Boolean isUrgent;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "batch_status_id")
	private BatchStatus batchStatus;

}
