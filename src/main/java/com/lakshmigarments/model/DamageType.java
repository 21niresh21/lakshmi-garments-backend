package com.lakshmigarments.model;

import java.util.Arrays;

public enum DamageType {
    REPAIRABLE,
    UNREPAIRABLE,
    SUPPLIER_DAMAGE;

    /**
     * Converts a String to DamageType safely (case-insensitive).
     */
    public static DamageType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DamageType cannot be null or empty");
        }

        return Arrays.stream(DamageType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid DamageType: " + value)
                );
    }
}
