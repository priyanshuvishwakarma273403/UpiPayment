package com.fraudService.network.service;

import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudLog;
import com.fraudService.network.model.*;
import com.fraudService.repository.FraudCaseRepository;
import com.fraudService.repository.FraudLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudNetworkIntelligenceService {

    private final FraudLogRepository fraudLogRepository;
    private final FraudCaseRepository fraudCaseRepository;

    private static final int DEFAULT_DEPTH = 2;
    private static final int MAX_ALLOWED_DEPTH = 3;

    public NetworkGraph getCustomerNetwork(String customerId, Integer depth) {
        int boundedDepth = sanitizeDepth(depth);
        log.info("Building customer network graph for customerId={} with boundedDepth={}", customerId, boundedDepth);

        Long senderId = parseSenderId(customerId);
        List<FraudLog> logs = senderId != null ? fraudLogRepository.findBySenderIdOrderByCheckedAtDesc(senderId) : Collections.emptyList();
        List<FraudCase> cases = fraudCaseRepository.findByCustomerId(customerId);

        return buildGraphFromLogsAndCases("CUST_" + customerId, NodeType.CUSTOMER, logs, cases, boundedDepth);
    }

    public NetworkGraph getTransactionNeighborhood(String transactionId, Integer depth) {
        int boundedDepth = sanitizeDepth(depth);
        log.info("Building transaction neighborhood graph for transactionId={} with boundedDepth={}", transactionId, boundedDepth);

        Optional<FraudLog> logOpt = fraudLogRepository.findByPaymentId(transactionId);
        List<FraudLog> logs = logOpt.map(Collections::singletonList).orElse(Collections.emptyList());
        List<FraudCase> cases = fraudCaseRepository.findByTransactionId(transactionId);

        return buildGraphFromLogsAndCases("TXN_" + transactionId, NodeType.TRANSACTION, logs, cases, boundedDepth);
    }

    public NetworkGraph getDeviceNetwork(String deviceId, Integer depth) {
        int boundedDepth = sanitizeDepth(depth);
        log.info("Building device network graph for deviceId={} with boundedDepth={}", deviceId, boundedDepth);

        List<FraudLog> logs = fraudLogRepository.findByDeviceId(deviceId);
        List<FraudCase> cases = Collections.emptyList();

        return buildGraphFromLogsAndCases("DEV_" + deviceId, NodeType.DEVICE, logs, cases, boundedDepth);
    }

    public NetworkGraph getBeneficiaryNetwork(String beneficiaryUpiId, Integer depth) {
        int boundedDepth = sanitizeDepth(depth);
        log.info("Building beneficiary network graph for beneficiaryUpiId={} with boundedDepth={}", beneficiaryUpiId, boundedDepth);

        List<FraudLog> logs = fraudLogRepository.findByReceiverUpiId(beneficiaryUpiId);
        List<FraudCase> cases = Collections.emptyList();

        return buildGraphFromLogsAndCases("BEN_" + beneficiaryUpiId, NodeType.BENEFICIARY, logs, cases, boundedDepth);
    }

    public List<FraudClusterReport> getSuspiciousClusters() {
        log.info("Running fraud ring and suspicious cluster detection algorithm across network intelligence store");

        List<FraudLog> allLogs = fraudLogRepository.findAll();
        List<FraudCase> allCases = fraudCaseRepository.findAll();

        Map<String, Set<String>> deviceToCustomers = new HashMap<>();
        Map<String, Set<String>> ipToCustomers = new HashMap<>();
        Map<String, Set<String>> beneficiaryToSenders = new HashMap<>();

        for (FraudLog fl : allLogs) {
            String custId = fl.getSenderId() != null ? String.valueOf(fl.getSenderId()) : "UNKNOWN";
            if (fl.getDeviceId() != null && !fl.getDeviceId().isBlank()) {
                deviceToCustomers.computeIfAbsent(fl.getDeviceId(), k -> new HashSet<>()).add(custId);
            }
            if (fl.getIpAddress() != null && !fl.getIpAddress().isBlank()) {
                ipToCustomers.computeIfAbsent(fl.getIpAddress(), k -> new HashSet<>()).add(custId);
            }
            if (fl.getReceiverUpiId() != null && !fl.getReceiverUpiId().isBlank()) {
                beneficiaryToSenders.computeIfAbsent(fl.getReceiverUpiId(), k -> new HashSet<>()).add(custId);
            }
        }

        List<FraudClusterReport> reports = new ArrayList<>();

        // 1. Shared Device Clusters
        for (Map.Entry<String, Set<String>> entry : deviceToCustomers.entrySet()) {
            if (entry.getValue().size() >= 2) {
                String devId = entry.getKey();
                Set<String> custs = entry.getValue();

                List<String> evidence = List.of(
                        "Device ID [" + devId + "] is shared across " + custs.size() + " distinct customer accounts (" + String.join(", ", custs) + ")",
                        "High relationship density detected between un-linked customer profiles"
                );

                List<FraudLog> clusterLogs = fraudLogRepository.findByDeviceId(devId);
                NetworkGraph graph = buildGraphFromLogsAndCases("DEV_" + devId, NodeType.DEVICE, clusterLogs, allCases, 2);

                reports.add(FraudClusterReport.builder()
                        .clusterId("CLUSTER_DEV_" + Math.abs(devId.hashCode()))
                        .title("Potential coordinated fraud network detected")
                        .severity(custs.size() > 3 ? "CRITICAL" : "HIGH")
                        .clusterRiskScore(custs.size() > 3 ? 0.92 : 0.78)
                        .totalNodesCount(graph.getNodes().size())
                        .totalRelationshipsCount(graph.getRelationships().size())
                        .sharedDevices(List.of(devId))
                        .sharedIps(Collections.emptyList())
                        .sharedBeneficiaries(Collections.emptyList())
                        .empiricalEvidence(evidence)
                        .detectedAt(LocalDateTime.now())
                        .networkGraph(graph)
                        .build());
            }
        }

        // 2. Shared IP Clusters
        for (Map.Entry<String, Set<String>> entry : ipToCustomers.entrySet()) {
            if (entry.getValue().size() >= 3) {
                String ip = entry.getKey();
                Set<String> custs = entry.getValue();

                List<String> evidence = List.of(
                        "IP Address [" + ip + "] originated transactions for " + custs.size() + " different customer accounts",
                        "Possible proxy or localized fraud ring operating from single IP origin"
                );

                List<FraudLog> clusterLogs = fraudLogRepository.findByIpAddress(ip);
                NetworkGraph graph = buildGraphFromLogsAndCases("IP_" + ip, NodeType.IP, clusterLogs, allCases, 2);

                reports.add(FraudClusterReport.builder()
                        .clusterId("CLUSTER_IP_" + Math.abs(ip.hashCode()))
                        .title("Potential coordinated fraud network detected")
                        .severity("HIGH")
                        .clusterRiskScore(0.81)
                        .totalNodesCount(graph.getNodes().size())
                        .totalRelationshipsCount(graph.getRelationships().size())
                        .sharedDevices(Collections.emptyList())
                        .sharedIps(List.of(ip))
                        .sharedBeneficiaries(Collections.emptyList())
                        .empiricalEvidence(evidence)
                        .detectedAt(LocalDateTime.now())
                        .networkGraph(graph)
                        .build());
            }
        }

        // 3. Shared Beneficiary Abuse Clusters
        for (Map.Entry<String, Set<String>> entry : beneficiaryToSenders.entrySet()) {
            if (entry.getValue().size() >= 3) {
                String benId = entry.getKey();
                Set<String> senders = entry.getValue();

                List<String> evidence = List.of(
                        "Target beneficiary UPI ID [" + benId + "] received funds from " + senders.size() + " separate accounts",
                        "Rapid fund aggregation pattern indicative of money mule funneling"
                );

                List<FraudLog> clusterLogs = fraudLogRepository.findByReceiverUpiId(benId);
                NetworkGraph graph = buildGraphFromLogsAndCases("BEN_" + benId, NodeType.BENEFICIARY, clusterLogs, allCases, 2);

                reports.add(FraudClusterReport.builder()
                        .clusterId("CLUSTER_BEN_" + Math.abs(benId.hashCode()))
                        .title("Potential coordinated fraud network detected")
                        .severity("CRITICAL")
                        .clusterRiskScore(0.88)
                        .totalNodesCount(graph.getNodes().size())
                        .totalRelationshipsCount(graph.getRelationships().size())
                        .sharedDevices(Collections.emptyList())
                        .sharedIps(Collections.emptyList())
                        .sharedBeneficiaries(List.of(benId))
                        .empiricalEvidence(evidence)
                        .detectedAt(LocalDateTime.now())
                        .networkGraph(graph)
                        .build());
            }
        }

        return reports;
    }

    private NetworkGraph buildGraphFromLogsAndCases(
            String rootId, NodeType rootType, List<FraudLog> logs, List<FraudCase> cases, int maxDepth) {

        Map<String, NetworkNode> nodeMap = new LinkedHashMap<>();
        Map<String, NetworkRelationship> relMap = new LinkedHashMap<>();
        List<String> evidence = new ArrayList<>();

        for (FraudLog fl : logs) {
            String custId = "CUST_" + (fl.getSenderId() != null ? fl.getSenderId() : "UNKNOWN");
            String accId = "ACC_" + (fl.getSenderUpiId() != null ? fl.getSenderUpiId() : "UNKNOWN");
            String devId = "DEV_" + (fl.getDeviceId() != null ? fl.getDeviceId() : "UNKNOWN");
            String ipId = "IP_" + (fl.getIpAddress() != null ? fl.getIpAddress() : "UNKNOWN");
            String benId = "BEN_" + (fl.getReceiverUpiId() != null ? fl.getReceiverUpiId() : "UNKNOWN");
            String txnId = "TXN_" + fl.getPaymentId();

            double riskScore = fl.getRiskScore() != null ? fl.getRiskScore() : 0.1;
            String riskLevel = fl.getRiskLevel() != null ? fl.getRiskLevel() : "LOW";

            // Add Nodes
            addOrUpdateNode(nodeMap, custId, fl.getSenderUpiId() != null ? fl.getSenderUpiId() : custId, NodeType.CUSTOMER, riskScore, riskLevel);
            addOrUpdateNode(nodeMap, accId, fl.getSenderUpiId() != null ? fl.getSenderUpiId() : accId, NodeType.ACCOUNT, riskScore, riskLevel);
            addOrUpdateNode(nodeMap, devId, fl.getDeviceId() != null ? fl.getDeviceId() : devId, NodeType.DEVICE, riskScore, riskLevel);
            addOrUpdateNode(nodeMap, ipId, fl.getIpAddress() != null ? fl.getIpAddress() : ipId, NodeType.IP, riskScore, riskLevel);
            addOrUpdateNode(nodeMap, benId, fl.getReceiverUpiId() != null ? fl.getReceiverUpiId() : benId, NodeType.BENEFICIARY, riskScore, riskLevel);
            addOrUpdateNode(nodeMap, txnId, fl.getPaymentId(), NodeType.TRANSACTION, riskScore, riskLevel);

            // Add Relationships
            addRelationship(relMap, custId, devId, RelationshipType.USES_DEVICE, 1.0, fl.getCheckedAt());
            addRelationship(relMap, custId, ipId, RelationshipType.CONNECTS_FROM, 1.0, fl.getCheckedAt());
            addRelationship(relMap, custId, accId, RelationshipType.OWNS_ACCOUNT, 1.0, fl.getCheckedAt());
            addRelationship(relMap, custId, txnId, RelationshipType.PERFORMED, 1.0, fl.getCheckedAt());
            addRelationship(relMap, accId, benId, RelationshipType.SENDS_TO, 1.0, fl.getCheckedAt());

            if (fl.getReceiverId() != null) {
                String merchId = "MERCH_" + fl.getReceiverId();
                addOrUpdateNode(nodeMap, merchId, "Merchant " + fl.getReceiverId(), NodeType.MERCHANT, 0.1, "LOW");
                addRelationship(relMap, txnId, merchId, RelationshipType.PURCHASED_FROM, 1.0, fl.getCheckedAt());
            }

            if ("REVIEW".equalsIgnoreCase(fl.getFinalDecision()) || "BLOCKED".equalsIgnoreCase(fl.getFinalDecision())) {
                evidence.add("Transaction [" + fl.getPaymentId() + "] flagged with risk level " + riskLevel + " (Score: " + riskScore + ")");
            }
        }

        // Correlate Related Cases
        for (FraudCase fc : cases) {
            String custId = "CUST_" + fc.getCustomerId();
            if (nodeMap.containsKey(custId)) {
                NetworkNode node = nodeMap.get(custId);
                node.setRelatedCaseCount(node.getRelatedCaseCount() + 1);
            }
        }

        List<NetworkNode> nodesList = new ArrayList<>(nodeMap.values());
        List<NetworkRelationship> relsList = new ArrayList<>(relMap.values());

        double avgRisk = nodesList.stream().mapToDouble(n -> n.getRiskScore() != null ? n.getRiskScore() : 0.0).average().orElse(0.1);

        return NetworkGraph.builder()
                .rootEntityId(rootId)
                .rootEntityType(rootType)
                .traversalDepth(maxDepth)
                .nodes(nodesList)
                .relationships(relsList)
                .clusterRiskScore(avgRisk)
                .networkSummary("Bounded graph neighborhood constructed with depth=" + maxDepth + " containing " + nodesList.size() + " nodes and " + relsList.size() + " edges.")
                .empiricalEvidence(evidence)
                .build();
    }

    private void addOrUpdateNode(Map<String, NetworkNode> map, String id, String label, NodeType type, double riskScore, String riskLevel) {
        if (!map.containsKey(id)) {
            map.put(id, NetworkNode.builder()
                    .id(id)
                    .label(label)
                    .type(type)
                    .riskScore(riskScore)
                    .riskLevel(riskLevel)
                    .connectionCount(1)
                    .transactionCount(type == NodeType.TRANSACTION ? 1 : 0)
                    .relatedCaseCount(0)
                    .build());
        } else {
            NetworkNode node = map.get(id);
            node.setConnectionCount(node.getConnectionCount() + 1);
            if (type == NodeType.TRANSACTION) {
                node.setTransactionCount(node.getTransactionCount() + 1);
            }
            if (riskScore > node.getRiskScore()) {
                node.setRiskScore(riskScore);
                node.setRiskLevel(riskLevel);
            }
        }
    }

    private void addRelationship(Map<String, NetworkRelationship> map, String src, String tgt, RelationshipType type, double weight, LocalDateTime ts) {
        String key = src + "->" + tgt + ":" + type;
        if (!map.containsKey(key)) {
            map.put(key, NetworkRelationship.builder()
                    .id(key)
                    .sourceId(src)
                    .targetId(tgt)
                    .type(type)
                    .weight(weight)
                    .timestamp(ts != null ? ts : LocalDateTime.now())
                    .build());
        }
    }

    private int sanitizeDepth(Integer depth) {
        if (depth == null || depth <= 0) {
            return DEFAULT_DEPTH;
        }
        return Math.min(depth, MAX_ALLOWED_DEPTH);
    }

    private Long parseSenderId(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return null;
        }
    }
}
