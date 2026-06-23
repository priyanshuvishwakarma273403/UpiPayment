package com.upimesh.loan.model.response;

import com.upimesh.loan.model.entity.LoanApplication;
import com.upimesh.loan.model.entity.LoanRepayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailsResponse {
    private LoanApplication application;
    private List<LoanRepayment> schedule;
}
