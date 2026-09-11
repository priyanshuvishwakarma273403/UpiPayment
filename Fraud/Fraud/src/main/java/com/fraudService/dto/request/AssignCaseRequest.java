package com.fraudService.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignCaseRequest {

    @NotBlank(message = "assignedTo is required")
    private String assignedTo;

    private String comment;
}
