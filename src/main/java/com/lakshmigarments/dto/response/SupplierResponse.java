package com.lakshmigarments.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for returning supplier information to clients.
 * Contains essential supplier details without sensitive information.
 */
@Getter
@Setter
public class SupplierResponse {

    private Long id;
    private String name;
    private String location;
    
    // Audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;

}
