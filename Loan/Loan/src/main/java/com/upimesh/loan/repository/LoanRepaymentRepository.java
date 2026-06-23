package com.upimesh.loan.repository;

import com.upimesh.loan.model.entity.LoanRepayment;
import com.upimesh.loan.model.enums.RepaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByApplicationId(String applicationId);
    Optional<LoanRepayment> findByApplicationIdAndEmiNumber(String applicationId, int emiNumber);
    List<LoanRepayment> findByStatusAndDueDateBefore(RepaymentStatus status, LocalDate date);
}
