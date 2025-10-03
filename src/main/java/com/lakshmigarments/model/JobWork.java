package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "jobworks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Batch batch;

    @ManyToOne
    private Item item;

    private Long quantity;

    @CreationTimestamp
    private Timestamp startedAt;

    @Column(nullable = true)
    private Timestamp endedAt;

    private String remarks;
}
