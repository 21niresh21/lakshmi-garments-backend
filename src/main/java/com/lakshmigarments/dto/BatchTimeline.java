package com.lakshmigarments.dto;

import java.util.List;

import com.lakshmigarments.model.BatchStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchTimeline {
	
	private BatchResponseDTO batchDetails;
	private List<BatchTimelineDetail> timelineDetail;

}
