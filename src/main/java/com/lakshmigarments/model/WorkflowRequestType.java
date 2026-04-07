package com.lakshmigarments.model;

public enum WorkflowRequestType {
	JOBWORK_RECEIPT;
	
	public static WorkflowRequestType from(String value) {
	    try {
	        return WorkflowRequestType.valueOf(value.toUpperCase());
	    } catch (Exception e) {
	        throw new IllegalArgumentException("Invalid WorkflowRequestType: " + value);
	    }
	}

}
