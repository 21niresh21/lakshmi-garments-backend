package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillRequest {

    @NotBlank(message = "Skill name is mandatory")
    @Size(max = 200, message = "Skill name should be less than 200 characters")
    private String name;

}
