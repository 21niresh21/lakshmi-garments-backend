package com.lakshmigarments.model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices", uniqueConstraints = { @UniqueConstraint(columnNames = { "invoice_number", "supplier_id" }) })
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends BaseAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String invoiceNumber;

	private LocalDate invoiceDate;

	private LocalDate receivedDate;

	@ManyToOne
	private Supplier supplier;

	@ManyToOne
	private Transport transport;

	private Double transportCost;

	@ColumnDefault("false")
	private Boolean isPaid;

	@OneToMany(mappedBy = "invoice")
	private List<LorryReceipt> lorryReceipts;

}
