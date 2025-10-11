package com.lakshmigarments.dto;

import com.lakshmigarments.model.Jobwork;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TicketRequestDTO {
    @NotNull
    @NotEmpty
    private List<JobworkDetailDTO> jobworksList;
}
