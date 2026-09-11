"""
Unit Test Suite for SentinelX ML Service
"""

import unittest
from ml_engine import SentinelXMlEngine
from dataset import generate_synthetic_sentinelx_dataset, DATASET_LABEL

class TestSentinelXMlEngine(unittest.TestCase):

    def setUp(self):
        self.engine = SentinelXMlEngine()

    def test_dataset_generator(self):
        df = generate_synthetic_sentinelx_dataset(100)
        self.assertEqual(len(df), 100)
        self.assertIn("amount_deviation", df.columns)
        self.assertIn("is_fraud", df.columns)
        self.assertEqual(DATASET_LABEL, "SYNTHETIC DEVELOPMENT DATA")

    def test_model_metrics_evaluation(self):
        metrics = self.engine.get_metrics()
        self.assertIn("primaryMetrics", metrics)
        primary = metrics["primaryMetrics"]
        self.assertIn("precision", primary)
        self.assertIn("recall", primary)
        self.assertIn("f1Score", primary)
        self.assertIn("rocAuc", primary)
        self.assertIn("prAuc", primary)
        self.assertGreaterEqual(primary["rocAuc"], 0.70)

    def test_predict_high_risk_vector(self):
        high_risk_payload = {
            "amount_deviation": 10.0,
            "velocity_1m": 8.0,
            "device_age_days": 0.5,
            "account_age_days": 2.0,
            "merchant_risk_score": 0.9,
            "beneficiary_history_count": 0.0,
            "behavioral_deviation": 0.85,
            "graph_cluster_density": 0.80
        }
        res = self.engine.predict(high_risk_payload)
        self.assertIn("fraudProbability", res)
        self.assertIn("modelVersion", res)
        self.assertIn("confidence", res)
        self.assertIn("featureContributions", res)
        self.assertGreater(res["fraudProbability"], 0.60)
        self.assertEqual(res["datasetLabel"], "SYNTHETIC DEVELOPMENT DATA")

    def test_predict_low_risk_vector(self):
        low_risk_payload = {
            "amount_deviation": 1.0,
            "velocity_1m": 1.0,
            "device_age_days": 300.0,
            "account_age_days": 500.0,
            "merchant_risk_score": 0.1,
            "beneficiary_history_count": 15.0,
            "behavioral_deviation": 0.05,
            "graph_cluster_density": 0.02
        }
        res = self.engine.predict(low_risk_payload)
        self.assertLess(res["fraudProbability"], 0.40)

if __name__ == "__main__":
    unittest.main()
