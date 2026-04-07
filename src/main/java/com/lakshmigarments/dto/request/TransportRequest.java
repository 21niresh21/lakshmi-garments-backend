package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransportRequest {

    @NotBlank(message = "Transport name is mandatory")
    @Size(max = 200, message = "Transport name must be less than 200 characters")
    private String name;

}
