package com.transaction_service.repository.mongo;

import com.transaction_service.entity.mongo.PaymentLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentLogRepository extends MongoRepository<PaymentLog, String> {

    Optional<PaymentLog> findByPaymentId(String paymentId);

    List<PaymentLog> findBySenderIdOrderByPaymentTimestampDesc(Long senderId);

    List<PaymentLog> findByReceiverIdOrderByPaymentTimestampDesc(Long receiverId);

    @Query("{ 'paymentStatus': ?0, 'paymentTimestamp': { $gte: ?1 } }")
    List<PaymentLog> findByStatusSince(String status, LocalDateTime since);

    long countByPaymentStatus(String status);


}
