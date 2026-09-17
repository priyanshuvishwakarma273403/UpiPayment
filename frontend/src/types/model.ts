import { ContractStatus } from './api';

export interface ModelPrediction {
  fraudProbability: number;
  modelVersion: string;
  confidence: number;
  featureContributions: Record<string, number>;
  datasetLabel: string;
  _contractStatus?: ContractStatus;
}
