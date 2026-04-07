package com.lakshmigarments.dto.request;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Employee name is mandatory")
    @Size(max = 200, message = "Employee name should be less than 200 characters")
    private String name;

    private List<Long> skills;

}
