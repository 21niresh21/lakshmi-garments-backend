package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batch_sub_categories", uniqueConstraints = @UniqueConstraint(columnNames = { "batch_id",
		"sub_category_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchSubCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long quantity;

	@ManyToOne
	@JoinColumn(name = "sub_category_id", nullable = false)
	private SubCategory subCategory;

	@ManyToOne
	@JoinColumn(name = "batch_id", nullable = false)
	private Batch batch;
}
