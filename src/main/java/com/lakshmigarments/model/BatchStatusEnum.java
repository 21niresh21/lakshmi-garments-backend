package com.lakshmigarments.model;

public enum BatchStatusEnum {
	CREATED("Created"),
	WIP("Work In Progress"),
	PACKAGED("Packaged"),
	DISCARDED("Discarded");
	
	private final String value;
	
	BatchStatusEnum(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}
