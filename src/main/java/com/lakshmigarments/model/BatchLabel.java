package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;


@Entity
@Table(name = "batch_labels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Batch batch;

    @ManyToOne
    private Employee collectedBy;

    @CreationTimestamp
    private Timestamp collectedAt;

    private String remarks;
}
