package com.upimesh.reconciliation.repository;

import com.upimesh.reconciliation.model.entity.ReconciliationDiscrepancy;
import com.upimesh.reconciliation.model.enums.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationDiscrepancyRepository extends JpaRepository<ReconciliationDiscrepancy, Long> {

    List<ReconciliationDiscrepancy> findByReportId(String reportId);

    Optional<ReconciliationDiscrepancy> findByDiscrepancyId(String discrepancyId);

    Optional<ReconciliationDiscrepancy> findByTransactionId(String transactionId);

    List<ReconciliationDiscrepancy> findByReportIdAndStatus(String reportId, ReconciliationStatus status);

    long countByReportIdAndStatus(String reportId, ReconciliationStatus status);

    @Query("SELECT d FROM ReconciliationDiscrepancy d WHERE d.status != com.upimesh.reconciliation.model.enums.ReconciliationStatus.RESOLVED")
    List<ReconciliationDiscrepancy> findUnresolvedDiscrepancies();
}
