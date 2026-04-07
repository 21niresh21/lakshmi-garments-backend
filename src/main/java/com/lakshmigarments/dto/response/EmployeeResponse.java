package com.lakshmigarments.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponse {

    private Long id;
    private String name;
    private List<SkillResponse> skills;

}
