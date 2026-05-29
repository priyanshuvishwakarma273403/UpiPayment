package com.walletService.repository;

import com.walletService.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);
    Optional<Wallet> findByUpiId(String upiId);
    boolean existsByUserId(Long upiId);

    //total platform balance (monitoring k liye)
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.isActive = true")
    BigDecimal getTotalPlatformBalance();



}
