package com.upimesh.dispute.repository;

import com.upimesh.dispute.model.entity.DisputeEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, Long> {
    List<DisputeEvidence> findByDisputeId(String disputeId);
}
