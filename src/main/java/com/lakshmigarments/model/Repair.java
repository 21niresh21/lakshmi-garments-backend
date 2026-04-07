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
@Table(name = "repairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repair {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private Damage damage;
	
	private Long quantity;
	
	@ManyToOne
	private Jobwork reworkJob;
	
	@CreationTimestamp
	private LocalDateTime createdAt;

}
