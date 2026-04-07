package com.lakshmigarments.exception;

public class InsufficientBatchQuantityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InsufficientBatchQuantityException(String message) {
		super(message);
	}
	
}
