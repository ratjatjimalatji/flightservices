package com.bharath.flightreservation.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String resource, Long id) {
		super(resource + " not found with id " + id);
	}

}
