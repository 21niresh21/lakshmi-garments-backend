package com.lakshmigarments.exception;

public class DuplicateSupplierException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public DuplicateSupplierException(String message) {
	        super(message);
	    }
}
