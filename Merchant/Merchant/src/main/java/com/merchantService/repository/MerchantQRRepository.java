package com.merchantService.repository;

import com.merchantService.entity.MerchantQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantQRRepository extends JpaRepository<MerchantQR,Long> {

    List<MerchantQR> findByMerchantIdAndIsActiveTrue(Long merchantId);
    Optional<MerchantQR> findByQrReferenceId(String qrReferenceId);
    List<MerchantQR>    findByMerchantIdAndQrType(Long merchantId, MerchantQR.QrType qrType);

}
