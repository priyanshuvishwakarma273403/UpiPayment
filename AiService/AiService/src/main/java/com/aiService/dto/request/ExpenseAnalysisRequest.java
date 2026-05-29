package com.aiService.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExpenseAnalysisRequest {
    @NotNull
    private Long userId;

    @Min(1)
    private int days = 30;  // Last N days ka analysis

}
