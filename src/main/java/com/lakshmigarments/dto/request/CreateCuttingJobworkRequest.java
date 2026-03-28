package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class CreateCuttingJobworkRequest extends CreateJobworkRequest {

	@NotNull
	private Long quantity;
}
