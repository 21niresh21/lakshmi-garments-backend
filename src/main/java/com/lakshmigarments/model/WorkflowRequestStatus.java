package com.lakshmigarments.model;

public enum WorkflowRequestStatus {

    PENDING,
    APPROVED,
    REJECTED;

    public static WorkflowRequestStatus from(String value) {
        try {
            return WorkflowRequestStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid WorkflowRequestStatus: " + value);
        }
    }
}
