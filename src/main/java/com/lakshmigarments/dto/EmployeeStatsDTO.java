package com.lakshmigarments.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeStatsDTO {
	
	private String employeeName;
	private Boolean hasOtherJobs;
	private Long lifetimePieces;
	private Double averageJobTime;
	private Double pendingJobs;

}
