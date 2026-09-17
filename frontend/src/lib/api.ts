import { apiClient } from './api/client';

export * from './api/client';
export * from './api/auth-client';
export * from './api/fraud-client';
export * from './api/risk-client';
export * from './api/transaction-client';
export * from './api/case-client';
export * from './api/ai-client';
export * from './api/analytics-client';
export * from './api/customer-client';
export * from './api/merchant-client';
export * from './api/network-client';
export * from './api/notification-client';

// Dedicated helper wrappers for frontend operations
export async function executePayment(data: { senderUpiId: string; receiverUpiId: string; amount: number; remarks?: string }) {
  return apiClient.post<any>('/payment/pay', data);
}

export async function verifyPaymentChecksum(paymentId: string) {
  return apiClient.post<any>('/payment/verify', { paymentId });
}

export async function screenAmlEntity(data: { name: string; type?: string }) {
  return apiClient.post<any>('/aml/screen', data);
}

export async function resolveAmlAlert(id: string, resolution: string) {
  return apiClient.put<any>(`/aml/alert/${id}/resolve`, { resolution });
}

export async function calculateRiskScore(payload: any) {
  return apiClient.post<any>('/risk/score', payload);
}

export async function fetchRiskRules() {
  return apiClient.get<any[]>('/risk/rules');
}

export async function fetchUserRiskProfile(userId: string) {
  return apiClient.get<any>(`/risk/profile/${userId}`);
}

export async function fetchMlMetrics() {
  return apiClient.get<any>('/metrics');
}

export async function fetchCandidateModels() {
  return apiClient.get<any[]>('/models/candidates');
}

export async function approveModelVersion(version: string) {
  return apiClient.post<any>(`/models/approve`, { version });
}

export async function triggerContinuousTraining() {
  return apiClient.post<any>('/train/continuous');
}

export async function queryFraudPolicyRag(query: string) {
  return apiClient.post<any>('/fraud/policy/query', { query });
}

export async function fetchMcpTools() {
  return apiClient.get<any[]>('/fraud/mcp/tools');
}

export async function executeMcpTool(toolName: string, args: any) {
  return apiClient.post<any>('/fraud/mcp/execute', { toolName, args });
}

export async function runFraudSimulation(preset: string) {
  return apiClient.post<any>('/fraud/simulator/run', { preset });
}

export async function fetchAnalyticsOverview() {
  return apiClient.get<any>('/analytics/dashboard');
}

export async function fetchHighRiskFlags() {
  return apiClient.get<any[]>('/fraud/high-risk');
}

export async function fetchObservabilitySummary() {
  return apiClient.get<any>('/fraud/observability/metrics/summary');
}

export async function fetchTransactions() {
  return apiClient.get<any[]>('/payment/history');
}

export async function fetchTransactionById(id: string) {
  return apiClient.get<any>(`/payment/${id}`);
}

export async function fetchFraudSignals(paymentId: string) {
  return apiClient.get<any>(`/fraud/signals/${paymentId}`);
}

export async function fetchFraudCases() {
  return apiClient.get<any[]>('/fraud/cases');
}

export async function updateCaseStatus(id: string, status: string) {
  return apiClient.put<any>(`/fraud/cases/${id}/status`, { status });
}

export async function searchInvestigations(query: string) {
  return apiClient.get<any[]>(`/fraud/search`, { params: { query } });
}

export async function fetchFraudNetworkClusters() {
  return apiClient.get<any[]>('/fraud/network/clusters');
}

export async function chatWithAi(message: string) {
  return apiClient.post<any>('/ai/chat', { message });
}

export async function fetchFraudLogById(id: string) {
  return apiClient.get<any>(`/fraud/logs/${id}`);
}
