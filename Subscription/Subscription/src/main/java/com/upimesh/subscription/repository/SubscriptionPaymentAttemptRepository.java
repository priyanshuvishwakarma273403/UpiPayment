package com.upimesh.subscription.repository;

import com.upimesh.subscription.model.entity.SubscriptionPaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPaymentAttemptRepository extends JpaRepository<SubscriptionPaymentAttempt, Long> {
    Optional<SubscriptionPaymentAttempt> findByAttemptId(String attemptId);
    List<SubscriptionPaymentAttempt> findBySubscriptionId(String subscriptionId);
}
