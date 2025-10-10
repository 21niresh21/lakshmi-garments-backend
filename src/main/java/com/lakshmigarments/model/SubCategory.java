package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "sub_categories",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "category_id"})
    }
)
@Data
@NoArgsConstructor
public class SubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String name;

}
