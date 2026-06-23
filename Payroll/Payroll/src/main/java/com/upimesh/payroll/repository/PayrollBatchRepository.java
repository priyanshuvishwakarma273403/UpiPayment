package com.upimesh.payroll.repository;

import com.upimesh.payroll.model.entity.PayrollBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollBatchRepository extends JpaRepository<PayrollBatch, Long> {
    Optional<PayrollBatch> findByBatchId(String batchId);
    List<PayrollBatch> findByCompanyId(String companyId);
}
