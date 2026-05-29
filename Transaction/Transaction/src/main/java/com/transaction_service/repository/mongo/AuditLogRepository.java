package com.transaction_service.repository.mongo;

import com.transaction_service.entity.mongo.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByEntityIdAndEntityType(String entityId, String entityType);

    List<AuditLog> findByServiceNameAndTimestampAfter(String serviceName, LocalDateTime after);


}
