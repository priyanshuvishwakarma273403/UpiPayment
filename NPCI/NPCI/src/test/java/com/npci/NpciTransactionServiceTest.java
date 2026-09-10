package com.npci;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.npci.exception.*;
import com.npci.model.entity.UpiTransaction;
import com.npci.model.enums.TransactionStatus;
import com.npci.model.enums.TransactionType;
import com.npci.model.request.InitiateTransactionRequest;
import com.npci.model.response.TransactionResponse;
import com.npci.repository.UpiTransactionRepository;
import com.npci.service.NpciClient;
import com.npci.service.NpciTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NpciTransactionServiceTest {

    @Mock private UpiTransactionRepository transactionRepo;
    @Mock private NpciClient npciClient;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks
    private NpciTransactionService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "perTransactionMax", new BigDecimal("100000"));
        ReflectionTestUtils.setField(service, "dailyMax", new BigDecimal("200000"));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private InitiateTransactionRequest validRequest() {
        InitiateTransactionRequest req = new InitiateTransactionRequest();
        req.setSenderUpiId("9876543210@upimesh");
        req.setReceiverUpiId("merchant@hdfc");
        req.setAmount(new BigDecimal("500.00"));
        req.setType(TransactionType.P2P);
        req.setIdempotencyKey("idem-001");
        req.setMpinHash("hashedmpin123");
        return req;
    }

    @Test
    void shouldSucceedForValidTransaction() throws Exception {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(transactionRepo.getDailyTotal(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepo.findRecentDuplicate(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Mock NPCI success response
        ObjectNode npciResp = objectMapper.createObjectNode();
        npciResp.put("status", "SUCCESS");
        npciResp.put("responseCode", "00");
        npciResp.put("txnId", "NPCI123456");
        npciResp.put("rrn", "RRN123456");
        when(npciClient.initiateTransaction(any(), any(), any(), any(), any(), any()))
                .thenReturn(npciResp);

        TransactionResponse response = service.initiateTransaction(validRequest());

        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals("NPCI123456", response.getNpciTransactionId());
        assertNotNull(response.getTransactionId());
    }

    @Test
    void shouldRejectWhenAmountExceedsLimit() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        InitiateTransactionRequest req = validRequest();
        req.setAmount(new BigDecimal("200000")); // Above ₹1 lakh limit

        assertThrows(TransactionLimitExceededException.class,
                () -> service.initiateTransaction(req));
    }

    @Test
    void shouldRejectWhenDailyLimitExceeded() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        // Already spent ₹1.9 lakh today, trying to pay ₹500 more (total > ₹2 lakh)
        when(transactionRepo.getDailyTotal(any(), any())).thenReturn(new BigDecimal("199600"));

        assertThrows(DailyLimitExceededException.class,
                () -> service.initiateTransaction(validRequest()));
    }

    @Test
    void shouldReturnExistingTransactionForDuplicateIdempotencyKey() throws Exception {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(valueOps.get(anyString())).thenReturn("TXN_EXISTING_001");

        UpiTransaction existing = new UpiTransaction();
        existing.setTransactionId("TXN_EXISTING_001");
        existing.setStatus(TransactionStatus.SUCCESS);
        existing.setAmount(new BigDecimal("500.00"));
        existing.setSenderUpiId("9876543210@upimesh");
        existing.setReceiverUpiId("merchant@hdfc");
        existing.setType(TransactionType.P2P);

        when(transactionRepo.findByTransactionId("TXN_EXISTING_001"))
                .thenReturn(Optional.of(existing));

        TransactionResponse response = service.initiateTransaction(validRequest());

        assertEquals("TXN_EXISTING_001", response.getTransactionId());
        // NPCI should NOT be called again for duplicate request
        verify(npciClient, never()).initiateTransaction(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldHandleNpciDeclinedResponse() throws Exception {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(transactionRepo.getDailyTotal(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepo.findRecentDuplicate(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Z9 = Insufficient funds
        ObjectNode npciResp = objectMapper.createObjectNode();
        npciResp.put("status", "FAILURE");
        npciResp.put("responseCode", "Z9");
        npciResp.put("message", "Insufficient funds");
        when(npciClient.initiateTransaction(any(), any(), any(), any(), any(), any()))
                .thenReturn(npciResp);

        TransactionResponse response = service.initiateTransaction(validRequest());

        assertEquals(TransactionStatus.DECLINED, response.getStatus());
        assertEquals("Z9", response.getNpciResponseCode());
    }
}
