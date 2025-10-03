package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "damages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Damage {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private JobWork jobWork;

    private Integer quantity;

    @ManyToOne
    private DamageType damageType;

    @ManyToOne
    private JobWork reworkJobWork;

    @ManyToOne
    private JobWork reportedBy;
}
