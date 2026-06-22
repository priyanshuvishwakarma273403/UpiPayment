package com.upimesh.reconciliation.repository;

import com.upimesh.reconciliation.model.entity.ReconciliationReport;
import com.upimesh.reconciliation.model.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationReportRepository extends JpaRepository<ReconciliationReport, Long> {

    Optional<ReconciliationReport> findByReportId(String reportId);

    Optional<ReconciliationReport> findByReconciliationDate(LocalDate reconciliationDate);

    List<ReconciliationReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<ReconciliationReport> findByReconciliationDateBetween(LocalDate from, LocalDate to);
}
