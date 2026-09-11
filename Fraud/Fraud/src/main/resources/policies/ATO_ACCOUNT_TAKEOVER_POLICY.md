# SentinelX Operational Risk Policy — Account Takeover (ATO)

## Section 1: Scope and Risk Classification
Account Takeover (ATO) occurs when an unauthorized actor gains control over a customer's digital payment profile. Any sequence involving a new device login, immediate device fingerprint change, followed by rapid beneficiary addition or high-value fund movement within 5 minutes must be classified as **HIGH_RISK / ATO_SUSPECTED**.

## Section 2: Mandatory Investigation Workflow
1. **Empirical Telemetry Verification**: Verify whether the login IP address and device identifier match the customer's historical profile.
2. **Immediate Account Hold**: If the transaction amount exceeds ₹50,000.00 following a device change within 15 minutes, analysts must apply a temporary transaction hold on the sender account.
3. **Customer Out-of-Band Notification**: Dispatch an urgent OTP/push verification request to the registered mobile number.

## Section 3: Resolution Criteria
- **Confirmed Fraud**: If the customer confirms unrecognized access, escalate case to Fraud Resolution Unit, flag device ID in Graph Network database, and block beneficiary UPI ID.
- **False Positive**: If customer verifies transaction via voice/biometric challenge, lift hold and log analyst override reason.
