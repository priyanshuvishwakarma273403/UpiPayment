"""
===============================================================================
SentinelX MLflow Experiment & Model Lifecycle Tracker
===============================================================================
Tracks training metadata, dataset versioning, model metrics (Precision, Recall,
F1, ROC-AUC, PR-AUC), and model approval states (STAGING, APPROVED, DEPLOYED).
===============================================================================
"""

import time
from typing import Dict, Any, List, Optional
import datetime

class MLflowTracker:
    def __init__(self):
        self.experiments: List[Dict[str, Any]] = []
        self.active_model_version: str = "sentinelx-ml-v1.0.0"
        self._init_baseline_run()

    def _init_baseline_run(self):
        baseline_run = {
            "runId": "run_base_001",
            "modelVersion": "sentinelx-ml-v1.0.0",
            "datasetVersion": "ds_synthetic_v1.0.0",
            "trainingTimestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "status": "DEPLOYED",
            "approvedBy": "SYSTEM_INITIALIZER",
            "approvedAt": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "metrics": {
                "precision": 0.8925,
                "recall": 0.8410,
                "f1Score": 0.8660,
                "rocAuc": 0.9420,
                "prAuc": 0.9150
            },
            "featureValidation": {
                "schemaValid": True,
                "nullCount": 0,
                "classDistribution": {"0": 2550, "1": 450},
                "status": "PASSED"
            }
        }
        self.experiments.append(baseline_run)

    def log_run(self, model_version: str, dataset_version: str, metrics: Dict[str, float], feature_validation: Dict[str, Any]) -> Dict[str, Any]:
        run_id = f"run_{int(time.time())}"
        run_record = {
            "runId": run_id,
            "modelVersion": model_version,
            "datasetVersion": dataset_version,
            "trainingTimestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "status": "PENDING_APPROVAL",
            "approvedBy": None,
            "approvedAt": None,
            "metrics": metrics,
            "featureValidation": feature_validation
        }
        self.experiments.append(run_record)
        return run_record

    def get_candidate_models(self) -> List[Dict[str, Any]]:
        return [exp for exp in self.experiments if exp["status"] == "PENDING_APPROVAL"]

    def approve_model(self, model_version: str, approved_by: str = "HUMAN_ANALYST") -> Optional[Dict[str, Any]]:
        target_exp = None
        for exp in self.experiments:
            if exp["modelVersion"] == model_version:
                target_exp = exp
                break

        if not target_exp:
            return None

        # Deprecate current active model
        for exp in self.experiments:
            if exp["status"] == "DEPLOYED":
                exp["status"] = "ARCHIVED"

        target_exp["status"] = "DEPLOYED"
        target_exp["approvedBy"] = approved_by
        target_exp["approvedAt"] = datetime.datetime.now(datetime.timezone.utc).isoformat()
        self.active_model_version = model_version
        return target_exp

    def get_history(self) -> List[Dict[str, Any]]:
        return self.experiments

    def get_active_model_info(self) -> Optional[Dict[str, Any]]:
        for exp in self.experiments:
            if exp["modelVersion"] == self.active_model_version and exp["status"] == "DEPLOYED":
                return exp
        return self.experiments[-1] if self.experiments else None
