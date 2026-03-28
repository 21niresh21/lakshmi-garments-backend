package com.lakshmigarments.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lakshmigarments.model.JobworkType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "jobworkType",
	    visible = true
	)
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = CreateCuttingJobworkRequest.class, name = "CUTTING"),
	    @JsonSubTypes.Type(value = CreateItemBasedJobworkRequest.class, name = "STITCHING"),
	    @JsonSubTypes.Type(value = CreateItemBasedJobworkRequest.class, name = "PACKAGING")
	})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CreateJobworkRequest {
	
	@NotNull
	private String jobworkNumber;
	
	@NotNull
	private JobworkType jobworkType;
	
	@NotNull
	private String assignedTo;
	
	@NotNull
	private String batchSerialCode;
	
	private String remarks;

}
