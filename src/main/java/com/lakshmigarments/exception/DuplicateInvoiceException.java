package com.lakshmigarments.exception;

public class DuplicateInvoiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public DuplicateInvoiceException(String message) {
		super(message);
	}
	
}
