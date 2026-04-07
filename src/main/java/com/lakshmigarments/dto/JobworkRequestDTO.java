package com.lakshmigarments.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lakshmigarments.model.JobworkType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobworkRequestDTO {
	
	private String employeeName;
	private Long employeeId;
	private String batchSerialCode;
	private List<String> itemNames;
	private List<Long> quantities;
	private JobworkType jobworkType;
	private String jobworkNumber;
//	private Long assignedBy;
	private String remarks;

}
