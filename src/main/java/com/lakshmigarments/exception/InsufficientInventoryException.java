package com.lakshmigarments.exception;

public class InsufficientInventoryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InsufficientInventoryException(String message) {
		super(message);
	}
	
}
