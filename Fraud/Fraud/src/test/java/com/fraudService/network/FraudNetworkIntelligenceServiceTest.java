package com.fraudService.network;

import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudLog;
import com.fraudService.network.model.FraudClusterReport;
import com.fraudService.network.model.NetworkGraph;
import com.fraudService.network.model.NodeType;
import com.fraudService.network.service.FraudNetworkIntelligenceService;
import com.fraudService.repository.FraudCaseRepository;
import com.fraudService.repository.FraudLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudNetworkIntelligenceServiceTest {

    @Mock
    private FraudLogRepository fraudLogRepository;

    @Mock
    private FraudCaseRepository fraudCaseRepository;

    private FraudNetworkIntelligenceService networkIntelligenceService;

    @BeforeEach
    void setUp() {
        networkIntelligenceService = new FraudNetworkIntelligenceService(fraudLogRepository, fraudCaseRepository);
    }

    @Test
    void testGetCustomerNetworkBoundedDepth() {
        FraudLog log1 = FraudLog.builder()
                .paymentId("PAY1001")
                .senderId(101L)
                .senderUpiId("alice@upimesh")
                .receiverUpiId("bob@upimesh")
                .amount(new BigDecimal("1000.00"))
                .deviceId("DEV_KNOWN_1")
                .ipAddress("127.0.0.1")
                .riskScore(0.2)
                .riskLevel("LOW")
                .checkedAt(LocalDateTime.now())
                .build();

        when(fraudLogRepository.findBySenderIdOrderByCheckedAtDesc(101L)).thenReturn(List.of(log1));
        when(fraudCaseRepository.findByCustomerId("101")).thenReturn(Collections.emptyList());

        // Pass excessive depth 5 -> should be capped at 3
        NetworkGraph graph = networkIntelligenceService.getCustomerNetwork("101", 5);

        assertNotNull(graph);
        assertEquals(3, graph.getTraversalDepth()); // Capped to max allowed depth 3
        assertFalse(graph.getNodes().isEmpty());
        assertFalse(graph.getRelationships().isEmpty());

        // Verify root node
        assertTrue(graph.getNodes().stream().anyMatch(n -> n.getType() == NodeType.CUSTOMER));
    }

    @Test
    void testGetDeviceNetworkReturnsNodesAndEdges() {
        FraudLog log1 = FraudLog.builder()
                .paymentId("PAY1002")
                .senderId(102L)
                .deviceId("DEV_SHARED_99")
                .ipAddress("10.0.0.1")
                .receiverUpiId("merchant@upimesh")
                .riskScore(0.85)
                .riskLevel("HIGH")
                .checkedAt(LocalDateTime.now())
                .build();

        when(fraudLogRepository.findByDeviceId("DEV_SHARED_99")).thenReturn(List.of(log1));

        NetworkGraph graph = networkIntelligenceService.getDeviceNetwork("DEV_SHARED_99", 2);

        assertNotNull(graph);
        assertEquals(2, graph.getTraversalDepth());
        assertTrue(graph.getNodes().stream().anyMatch(n -> n.getType() == NodeType.DEVICE));
    }

    @Test
    void testDetectSuspiciousClustersWithSharedDevice() {
        FraudLog log1 = FraudLog.builder()
                .paymentId("PAY101")
                .senderId(201L)
                .deviceId("DEV_MULE_RING")
                .ipAddress("192.168.1.1")
                .receiverUpiId("mule@upimesh")
                .riskScore(0.80)
                .checkedAt(LocalDateTime.now())
                .build();

        FraudLog log2 = FraudLog.builder()
                .paymentId("PAY102")
                .senderId(202L) // Different customer, same device
                .deviceId("DEV_MULE_RING")
                .ipAddress("192.168.1.2")
                .receiverUpiId("mule@upimesh")
                .riskScore(0.90)
                .checkedAt(LocalDateTime.now())
                .build();

        when(fraudLogRepository.findAll()).thenReturn(List.of(log1, log2));
        when(fraudLogRepository.findByDeviceId("DEV_MULE_RING")).thenReturn(List.of(log1, log2));
        when(fraudCaseRepository.findAll()).thenReturn(Collections.emptyList());

        List<FraudClusterReport> clusters = networkIntelligenceService.getSuspiciousClusters();

        assertNotNull(clusters);
        assertFalse(clusters.isEmpty());

        FraudClusterReport report = clusters.get(0);
        assertEquals("Potential coordinated fraud network detected", report.getTitle());
        assertTrue(report.getSharedDevices().contains("DEV_MULE_RING"));
        assertFalse(report.getEmpiricalEvidence().isEmpty());
    }
}
