package com.lakshmigarments.model;

import java.util.Optional;

public enum JobworkType {
	CUTTING,
	EMBROIDERY,
	STITCHING,
    OVERLOCK,
    IRONING,
	PACKAGING;
	
	public static Optional<JobworkType> fromString(String value) {
        if (value == null) return Optional.empty();
        try {
            return Optional.of(JobworkType.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty(); // invalid value
        }
    }
}
