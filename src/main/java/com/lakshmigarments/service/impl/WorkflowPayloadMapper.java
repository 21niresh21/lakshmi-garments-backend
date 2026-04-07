package com.lakshmigarments.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakshmigarments.dto.request.CreateJobworkReceiptRequest;
import com.lakshmigarments.model.WorkflowRequest;
import org.springframework.stereotype.Component;

@Component
public class WorkflowPayloadMapper {

    private final ObjectMapper objectMapper;

    public WorkflowPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object readPayload(WorkflowRequest request) {
        try {
            if (request.getPayload() == null) {
                return null;
            }

            switch (request.getWorkflowRequestType()) {
                case JOBWORK_RECEIPT:
                    return objectMapper.readValue(
                        request.getPayload(), CreateJobworkReceiptRequest.class);

                default:
                    throw new IllegalStateException(
                        "Unsupported workflow type: " + request.getWorkflowRequestType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid payload", e);
        }
    }
}
