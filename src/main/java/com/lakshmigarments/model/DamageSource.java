package com.lakshmigarments.model;

import java.util.Arrays;

public enum DamageSource {
    CURRENT_JOBWORK,
    PREVIOUS_JOBWORK;

    /**
     * Converts a String to DamageSource safely (case-insensitive).
     */
    public static DamageSource fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DamageSource cannot be null or empty");
        }

        return Arrays.stream(DamageSource.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid DamageSource: " + value)
                );
    }
}
