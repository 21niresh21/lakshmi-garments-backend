package com.lakshmigarments.dto;

import java.util.List;

import com.lakshmigarments.dto.response.JobworkDetailDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkTimelineDTO {
	
	private JobworkDetailDTO jobworkDetail;
	private List<BatchTimelineDetail> timelineDetail;

}
