package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_categories")
@Data
@NoArgsConstructor
public class SubCategory extends BaseAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 200, unique = true, nullable = false)
	private String name;

}
