package com.upimesh.payroll.model.entity;

import com.upimesh.payroll.model.enums.EmployeePaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_payments", indexes = {
        @Index(name = "idx_emp_payment_id", columnList = "paymentId", unique = true),
        @Index(name = "idx_emp_pay_batch_id", columnList = "batchId"),
        @Index(name = "idx_emp_pay_employee_id", columnList = "employeeId"),
        @Index(name = "idx_emp_pay_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String paymentId;

    @Column(nullable = false, length = 40)
    private String batchId;

    @Column(nullable = false, length = 100)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String employeeName;

    @Column(nullable = false, length = 100)
    private String employeeUpiId;

    @Column(nullable = false, length = 30)
    private String bankAccount; // Masked bank account, e.g. "XXXXXX1234"

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal grossSalary;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal pfDeduction;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal esiDeduction;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal tdsDeduction;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal otherDeductions;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeePaymentStatus status;

    @Column(length = 60)
    private String transactionId;

    @Column(length = 255)
    private String failureReason;

    private LocalDateTime processedAt;
}
