package com.lakshmigarments.model;

public enum BatchStatus {
	CREATED("Created"),
	ASSIGNED("Assigned"),
	COMPLETED("Completed"),
	DISCARDED("Discarded"),
	CLOSED("Closed");
	
	private final String value;
	
	BatchStatus(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}
