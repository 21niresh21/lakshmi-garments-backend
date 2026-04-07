package com.lakshmigarments.dto;

import java.time.LocalDateTime;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchTimelineDetail {

	private String message;
	private String timeTakenFromPrevious;
	private String employeeName;
	private String jobworkNumber;
	private String transactionType;
	private LocalDateTime performedAt;
	private String stage;
	private Long totalQuantity;
	private List<TimelineItemDetail> items;
	
}
