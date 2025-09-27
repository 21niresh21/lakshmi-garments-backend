package com.lakshmigarments.exception;

public class DuplicateBatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateBatchException(String message) {
		super(message);
	}
	
}
