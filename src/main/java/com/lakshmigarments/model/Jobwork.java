package com.lakshmigarments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobworks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Jobwork extends BaseAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

//    @ManyToOne
//    private Employee employee;

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
	@Column(name = "jobwork_status", nullable = false, length = 100)
	private JobworkStatus jobworkStatus;

	@ManyToOne
	private Employee assignedTo;

	@ManyToOne
	@JoinColumn(name = "parent_jobwork_id")
	private Jobwork parentJobwork;

	private String remarks;

	@OneToMany(mappedBy = "jobwork", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<JobworkItem> jobworkItems = new ArrayList<>();

}
