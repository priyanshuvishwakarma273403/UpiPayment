package com.upimesh.rewards.repository;

import com.upimesh.rewards.model.entity.UserRewardAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRewardAccountRepository extends JpaRepository<UserRewardAccount, Long> {
    Optional<UserRewardAccount> findByUserId(String userId);
    Optional<UserRewardAccount> findByUserUpiId(String userUpiId);
    Optional<UserRewardAccount> findByReferralCode(String referralCode);
}
