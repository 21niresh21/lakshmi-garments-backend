package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobworks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jobwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Batch batch;

    @ManyToOne
    private Item item;

    @ManyToOne
    private JobworkType jobworkType;

    private Long quantity;

    private String jobworkNumber;

    @CreationTimestamp
    private LocalDateTime startedAt;

    @Column(nullable = true)
    private LocalDateTime endedAt;

    private String remarks;
}
