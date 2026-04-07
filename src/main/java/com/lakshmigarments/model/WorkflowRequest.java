package com.lakshmigarments.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workflow_requests")
@Data
@NoArgsConstructor
public class WorkflowRequest extends BaseAuditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "workflow_request_type", nullable = false, length = 50)
	private WorkflowRequestType workflowRequestType;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "workflow_request_status", nullable = false, length = 50)
	private WorkflowRequestStatus workflowRequestStatus;
	
	@Lob
	@Column(columnDefinition = "TEXT")
	private String payload;
	
	private String systemComments;
	
	private String remarks;
	
}
