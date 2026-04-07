package com.lakshmigarments.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobworkResponse {
	
	private String jobworkNumber;
	private String jobworkType;
	private String jobworkStatus;
	private String batchSerialCode;
	private LocalDateTime startedAt;
	private LocalDateTime updatedAt;
	private String remarks;
	private List<JobworkItemDetail> items;
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class JobworkItemDetail {
		private String itemName;
		private Long quantity;
	}
}
