package com.upimesh.bankgateway.repository;

import com.upimesh.bankgateway.model.entity.IfscDetail;
import com.upimesh.bankgateway.model.enums.BankCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IfscDetailRepository extends JpaRepository<IfscDetail, Long> {

    Optional<IfscDetail> findByIfscCode(String ifscCode);

    List<IfscDetail> findByBankCodeAndCity(BankCode bankCode, String city);

    boolean existsByIfscCode(String ifscCode);
}
