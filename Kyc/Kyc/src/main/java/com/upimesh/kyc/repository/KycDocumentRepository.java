package com.upimesh.kyc.repository;

import com.upimesh.kyc.model.entity.KycDocument;
import com.upimesh.kyc.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByKycId(String kycId);
    Optional<KycDocument> findByKycIdAndDocumentType(String kycId, DocumentType documentType);
}
