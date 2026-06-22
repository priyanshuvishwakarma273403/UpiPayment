package com.upimesh.bankgateway.repository;

import com.upimesh.bankgateway.model.entity.LinkedBankAccount;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.LinkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkedBankAccountRepository extends JpaRepository<LinkedBankAccount, Long> {

    Optional<LinkedBankAccount> findByAccountId(String accountId);

    List<LinkedBankAccount> findByUserUpiIdOrderByIsPrimaryDescLinkedAtDesc(String userUpiId);

    List<LinkedBankAccount> findByUserUpiIdAndStatus(String userUpiId, LinkStatus status);

    Optional<LinkedBankAccount> findByUserUpiIdAndIsPrimaryTrue(String userUpiId);

    boolean existsByUserUpiIdAndBankCodeAndStatus(String userUpiId, BankCode bankCode, LinkStatus status);

    long countByUserUpiIdAndStatus(String userUpiId, LinkStatus status);

    @Modifying
    @Query("UPDATE LinkedBankAccount l SET l.isPrimary = false WHERE l.userUpiId = :userUpiId")
    void clearAllPrimary(@Param("userUpiId") String userUpiId);

    Optional<LinkedBankAccount> findByUserUpiIdAndMaskedAccountNumber(String userUpiId, String maskedAccountNumber);
}
