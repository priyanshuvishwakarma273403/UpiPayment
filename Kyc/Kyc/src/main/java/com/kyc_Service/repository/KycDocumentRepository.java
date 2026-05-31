package com.kyc_Service.repository;

import com.kyc_Service.entity.KycDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// kycDocument MongoDb repository
@Repository
public interface KycDocumentRepository extends MongoRepository<KycDocument,String> {


    List<KycDocument> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<KycDocument> findByUserIdAndDocumentType(Long userId, String documentType);

    List<KycDocument> findByUserIdAndIsVerified(Long userId, Boolean isVerified);

}
