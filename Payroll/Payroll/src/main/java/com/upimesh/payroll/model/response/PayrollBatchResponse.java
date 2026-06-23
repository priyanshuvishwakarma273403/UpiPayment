package com.upimesh.payroll.model.response;

import com.upimesh.payroll.model.entity.EmployeePayment;
import com.upimesh.payroll.model.entity.PayrollBatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollBatchResponse {
    private PayrollBatch batch;
    private List<EmployeePayment> payments;
}
