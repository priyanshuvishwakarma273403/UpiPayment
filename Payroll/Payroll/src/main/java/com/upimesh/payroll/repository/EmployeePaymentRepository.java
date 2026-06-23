package com.upimesh.payroll.repository;

import com.upimesh.payroll.model.entity.EmployeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePaymentRepository extends JpaRepository<EmployeePayment, Long> {
    Optional<EmployeePayment> findByPaymentId(String paymentId);
    List<EmployeePayment> findByBatchId(String batchId);
}
