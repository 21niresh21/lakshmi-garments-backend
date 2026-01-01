package com.lakshmigarments.model;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jobwork_items")
@Data
@NoArgsConstructor
public class JobworkItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private Item item;
	
	@ManyToOne
	private Jobwork jobwork;
	
	private Long quantity;
	
    @Enumerated(EnumType.STRING)
    @Column(name = "jobwork_status", nullable = false)
    private JobworkItemStatus jobworkStatus;

}
