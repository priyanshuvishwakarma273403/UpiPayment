package com.paymentService.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Wallet Service ko call karne ke liye internal DTO
 * (Feign client request body)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDebitCreditRequest {

    private Long userId;
    private BigDecimal amount;
    private String paymentId;
    private String description;

}
