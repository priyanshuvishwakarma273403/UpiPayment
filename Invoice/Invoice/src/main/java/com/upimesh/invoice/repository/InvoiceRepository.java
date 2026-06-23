package com.upimesh.invoice.repository;

import com.upimesh.invoice.model.entity.Invoice;
import com.upimesh.invoice.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceId(String invoiceId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByMerchantUpiId(String merchantUpiId);
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);
    Optional<Invoice> findFirstByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);
}
