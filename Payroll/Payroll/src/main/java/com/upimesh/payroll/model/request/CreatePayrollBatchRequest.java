package com.upimesh.payroll.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayrollBatchRequest {

    @NotBlank(message = "Company ID is required")
    private String companyId;

    @NotBlank(message = "Company UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$", message = "Invalid UPI ID format")
    private String companyUpiId;

    @NotBlank(message = "Month is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Month must be in YYYY-MM format")
    private String month;

    @NotEmpty(message = "Employee list cannot be empty")
    @Valid
    private List<EmployeeSalaryInput> employees;
}
