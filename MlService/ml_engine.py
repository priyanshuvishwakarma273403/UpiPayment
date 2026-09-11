"""
===============================================================================
SentinelX ML Fraud Detection Engine — Continuous Learning Edition
===============================================================================
Trains XGBoost / Scikit-Learn fraud prediction models using investigator outcomes.
Evaluates Precision, Recall, F1, ROC-AUC, and PR-AUC metrics.
Logs runs to MLflow Tracker and enforces human model approval before deployment.
===============================================================================
"""

import numpy as np
import pandas as pd
import time
import datetime
from typing import Dict, Any, List, Tuple
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import precision_score, recall_score, f1_score, roc_auc_score, precision_recall_curve, auc

from dataset import generate_synthetic_sentinelx_dataset, DATASET_LABEL
from mlflow_tracker import MLflowTracker

MODEL_VERSION_PREFIX = "sentinelx-ml-v1."

class SentinelXMlEngine:
    def __init__(self):
        self.tracker = MLflowTracker()
        self.model = RandomForestClassifier(n_estimators=100, max_depth=8, random_state=42)
        self.candidate_model = None
        self.feature_names = [
            "amount_deviation",
            "velocity_1m",
            "device_age_days",
            "account_age_days",
            "merchant_risk_score",
            "beneficiary_history_count",
            "behavioral_deviation",
            "graph_cluster_density"
        ]
        self.metrics: Dict[str, float] = {}
        self.dataset_label = DATASET_LABEL
        self.current_model_version = "sentinelx-ml-v1.0.0"
        self._is_trained = False
        self.train_model()

    def train_model(self) -> Dict[str, float]:
        """
        Baseline model training on synthetic data.
        """
        df = generate_synthetic_sentinelx_dataset(num_samples=3000)
        X = df[self.feature_names]
        y = df["is_fraud"]

        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42, stratify=y)
        self.model.fit(X_train, y_train)

        y_pred = self.model.predict(X_test)
        y_proba = self.model.predict_proba(X_test)[:, 1]

        precision = float(precision_score(y_test, y_pred, zero_division=0))
        recall = float(recall_score(y_test, y_pred, zero_division=0))
        f1 = float(f1_score(y_test, y_pred, zero_division=0))
        roc_auc = float(roc_auc_score(y_test, y_proba))

        p_curve, r_curve, _ = precision_recall_curve(y_test, y_proba)
        pr_auc = float(auc(r_curve, p_curve))

        self.metrics = {
            "precision": round(precision, 4),
            "recall": round(recall, 4),
            "f1Score": round(f1, 4),
            "rocAuc": round(roc_auc, 4),
            "prAuc": round(pr_auc, 4),
            "datasetLabel": self.dataset_label
        }
        self._is_trained = True
        return self.metrics

    def validate_features(self, df: pd.DataFrame) -> Dict[str, Any]:
        """
        Step 3: Feature validation (schema, nulls, class distribution).
        """
        missing_cols = [col for col in self.feature_names if col not in df.columns]
        if missing_cols:
            return {"schemaValid": False, "reason": f"Missing columns: {missing_cols}", "status": "FAILED"}

        null_count = int(df[self.feature_names].isnull().sum().sum())
        class_dist = df["is_fraud"].value_counts().to_dict()
        class_dist_str = {str(k): int(v) for k, v in class_dist.items()}

        status = "PASSED" if null_count == 0 and len(class_dist) >= 2 else "FAILED"

        return {
            "schemaValid": True,
            "nullCount": null_count,
            "classDistribution": class_dist_str,
            "status": status
        }

    def train_continuous(self, labeled_samples: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Continuous Learning Pipeline: Ingests investigator labels (CONFIRMED_FRAUD=1.0, FALSE_POSITIVE=0.0),
        builds updated dataset, performs feature validation, trains candidate model, logs to MLflow,
        and leaves candidate model in PENDING_APPROVAL status (never auto-deploys).
        """
        # Generate baseline dataset and append new investigator labeled samples
        base_df = generate_synthetic_sentinelx_dataset(num_samples=2000)

        if labeled_samples:
            new_rows = []
            for sample in labeled_samples:
                fv = sample.get("featureVector", {})
                label_val = 1 if sample.get("status") == "CONFIRMED_FRAUD" else 0
                row = {name: float(fv.get(name, 0.0)) for name in self.feature_names}
                row["is_fraud"] = label_val
                new_rows.append(row)
            labeled_df = pd.DataFrame(new_rows)
            combined_df = pd.concat([base_df, labeled_df], ignore_index=True)
        else:
            combined_df = base_df

        # Step 3: Feature validation
        validation_res = self.validate_features(combined_df)
        if validation_res["status"] != "PASSED":
            raise ValueError(f"Feature validation failed: {validation_res}")

        # Step 4: Training & Evaluation
        X = combined_df[self.feature_names]
        y = combined_df["is_fraud"]

        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.25, random_state=42, stratify=y)

        candidate = RandomForestClassifier(n_estimators=120, max_depth=10, random_state=int(time.time()))
        candidate.fit(X_train, y_train)

        y_pred = candidate.predict(X_test)
        y_proba = candidate.predict_proba(X_test)[:, 1]

        precision = float(precision_score(y_test, y_pred, zero_division=0))
        recall = float(recall_score(y_test, y_pred, zero_division=0))
        f1 = float(f1_score(y_test, y_pred, zero_division=0))
        roc_auc = float(roc_auc_score(y_test, y_proba))

        p_curve, r_curve, _ = precision_recall_curve(y_test, y_proba)
        pr_auc = float(auc(r_curve, p_curve))

        new_metrics = {
            "precision": round(precision, 4),
            "recall": round(recall, 4),
            "f1Score": round(f1, 4),
            "rocAuc": round(roc_auc, 4),
            "prAuc": round(pr_auc, 4),
            "labeledSamplesCount": len(labeled_samples)
        }

        # Step 5: MLflow Registration
        version_num = len(self.tracker.experiments)
        new_version = f"{MODEL_VERSION_PREFIX}{version_num}.0"
        dataset_ver = f"ds_v{version_num}_{datetime.datetime.now().strftime('%Y%m%d')}"

        run_record = self.tracker.log_run(
            model_version=new_version,
            dataset_version=dataset_ver,
            metrics=new_metrics,
            feature_validation=validation_res
        )

        # Store candidate in memory awaiting human approval
        self.candidate_model = {
            "version": new_version,
            "modelObj": candidate,
            "metrics": new_metrics
        }

        return run_record

    def approve_and_deploy_model(self, model_version: str, approved_by: str = "HUMAN_ANALYST") -> Dict[str, Any]:
        """
        Step 6: Controlled Human Approval & Deployment
        """
        record = self.tracker.approve_model(model_version, approved_by)
        if not record:
            raise ValueError(f"Candidate model version {model_version} not found in MLflow registry")

        if self.candidate_model and self.candidate_model["version"] == model_version:
            self.model = self.candidate_model["modelObj"]
            self.current_model_version = model_version
            self.metrics = self.candidate_model["metrics"]
            self.candidate_model = None

        return record

    def predict(self, feature_vector: Dict[str, float]) -> Dict[str, Any]:
        if not self._is_trained:
            self.train_model()

        input_data = [feature_vector.get(name, 0.0) for name in self.feature_names]
        input_df = pd.DataFrame([input_data], columns=self.feature_names)

        prob_fraud = float(self.model.predict_proba(input_df)[0][1])
        prob_fraud = round(prob_fraud, 4)

        confidence = round(float(2.0 * abs(prob_fraud - 0.5)), 4)

        importances = self.model.feature_importances_
        feature_contributions = {}
        for name, imp in zip(self.feature_names, importances):
            val = feature_vector.get(name, 0.0)
            impact = round(float(imp * (val / (val + 1.0))), 4)
            feature_contributions[name] = impact

        return {
            "fraudProbability": prob_fraud,
            "modelVersion": self.current_model_version,
            "confidence": confidence,
            "featureContributions": feature_contributions,
            "datasetLabel": self.dataset_label
        }

    def get_metrics(self) -> Dict[str, Any]:
        active_info = self.tracker.get_active_model_info()
        return {
            "modelVersion": self.current_model_version,
            "primaryMetrics": self.metrics,
            "activeRun": active_info,
            "note": "Evaluation performed using Precision, Recall, F1, ROC-AUC, and PR-AUC."
        }
