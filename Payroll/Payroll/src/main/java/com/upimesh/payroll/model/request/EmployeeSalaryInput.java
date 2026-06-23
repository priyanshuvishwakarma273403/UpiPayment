package com.upimesh.payroll.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryInput {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Employee Name is required")
    private String employeeName;

    @NotBlank(message = "Employee UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$", message = "Invalid UPI ID format")
    private String employeeUpiId;

    @NotBlank(message = "Bank Account number is required")
    private String bankAccount;

    @NotNull(message = "Gross Salary is required")
    @DecimalMin(value = "100.00", message = "Minimum gross salary is ₹100")
    private BigDecimal grossSalary;
}
