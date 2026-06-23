package com.upimesh.rewards.repository;

import com.upimesh.rewards.model.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByOfferId(String offerId);
    List<Offer> findByMerchantUpiIdAndIsActiveTrue(String merchantUpiId);
}
