package com.lakshmigarments.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "jobwork_receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobworkReceipt {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	private Jobwork jobwork;
	
	@ManyToOne
	private Employee completedBy;
	
	@ManyToOne
	private User receivedBy;
	
	@CreationTimestamp
	private LocalDateTime receivedAt;

}
