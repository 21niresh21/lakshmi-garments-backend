package com.lakshmigarments.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchTimelineDetail {

	private String message;
	private LocalDateTime workedAt;
	private String workedBy;
	private String timeTakenFromPrevious;
	private String assignedBy;
	
	
}
