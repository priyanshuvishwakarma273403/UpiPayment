package com.merchantService.repository;

import com.merchantService.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByEmail(String email);
    Optional<Merchant> findByMerchantUpiId(String merchantUpiId);
    Optional<Merchant> findByMerchantCode(String merchantCode);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

}
