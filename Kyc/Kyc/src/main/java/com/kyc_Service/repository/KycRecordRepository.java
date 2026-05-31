package com.kyc_Service.repository;

import com.kyc_Service.entity.KycRecord;
import org.springframework.data.jpa.repository.JpaRepository;

// kycRecord Repository - mysql
public interface KycRecordRepository extends JpaRepository<KycRecord,Long> {


}
