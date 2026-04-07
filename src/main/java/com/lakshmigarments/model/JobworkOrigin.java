package com.lakshmigarments.model;

public enum JobworkOrigin {
    ORIGINAL,      // First assignment created from batch
    SPLIT,         // Created by splitting an existing jobwork
    REASSIGNED     // Reassigned after return / partial completion
}
