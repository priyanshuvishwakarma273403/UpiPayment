# SentinelX Operational Runbook — Money Mule & Beneficiary Abuse

## Section 1: Money Mule Network Detection
Money mule behavior is characterized by rapid pass-through fund transfers, multiple inbound payments from disparate senders consolidated into a single beneficiary, or accounts sharing IP/Device nodes with confirmed fraud clusters.

## Section 2: Beneficiary Blacklisting Protocol
1. **Cluster Threshold**: If a beneficiary account receives payments from $\ge 3$ distinct unlinked customer accounts within 10 minutes, automatically mark beneficiary status as **REQUIRES_INVESTIGATION**.
2. **Network Correlation**: Cross-reference beneficiary in Neo4j Graph Network. If beneficiary shares a device or IP with a previously blacklisted account, issue an immediate merchant/beneficiary freeze.

## Section 3: Recovery & Reporting
Report suspicious beneficiary clusters to National Payments Corporation of India (NPCI) Fraud Monitoring Cell within 24 hours.
