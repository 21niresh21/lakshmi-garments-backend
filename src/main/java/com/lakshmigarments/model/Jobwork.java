package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "jobwork_type", nullable = false)
    private JobworkType jobworkType;

    private String jobworkNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "jobwork_origin", nullable = false)
    private JobworkOrigin jobworkOrigin;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "jobwork_status", nullable = false, length = 50)
    private JobworkStatus jobworkStatus;
    
    @ManyToOne
    private User assignedBy;
    
    @ManyToOne
    private Jobwork reworkJobwork;
    
    @CreationTimestamp
    private LocalDateTime startedAt;

    private String remarks;
    
    @OneToMany(mappedBy = "jobwork", fetch = FetchType.LAZY)
    private List<JobworkItem> jobworkItems;

}
