package com.aiService.repository;

import com.aiService.entity.ChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {
    Optional<ChatHistory> findByUserIdAndSessionId(Long userId, String sessionId);
    List<ChatHistory> findByUserIdOrderByLastMessageAtDesc(Long userId);
}
