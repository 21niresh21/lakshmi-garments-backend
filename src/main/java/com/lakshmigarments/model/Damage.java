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
    private JobworkReceiptItem jobworkReceiptItem;

    private Long quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "damage_type", nullable = false)
    private DamageType damageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_source")
    private DamageSource damageSource;

    @ManyToOne
    private Jobwork reworkJobWork;

    @ManyToOne
    private Jobwork reportedFrom;

    @ManyToOne
    @JoinColumn(name = "caused_by_jobwork_id")
    private Jobwork causedBy;
}
