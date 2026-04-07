package com.lakshmigarments.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "jobwork_receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobworkReceipt extends BaseAuditable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne
	private Jobwork jobwork;
	
	@OneToMany(mappedBy = "jobworkReceipt", fetch = FetchType.LAZY, 
			cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobworkReceiptItem> jobworkReceiptItems;

}
