"""
===============================================================================
SentinelX ML Fraud Prediction & Continuous Learning Service (FastAPI)
===============================================================================
Exposes real-time fraud prediction, continuous model retraining, MLflow metric
tracking, and human model approval deployment gates.
===============================================================================
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Dict, Any, List, Optional

from ml_engine import SentinelXMlEngine

app = FastAPI(
    title="SentinelX ML Fraud Prediction & Continuous Learning Service",
    description="Independent Machine Learning Microservice for Fraud Risk Prediction and Continuous Learning",
    version="2.0.0"
)

ml_engine = SentinelXMlEngine()

class FeatureVectorRequest(BaseModel):
    amount_deviation: float = Field(..., description="Transaction amount ratio vs user historical average")
    velocity_1m: float = Field(..., description="1-minute transaction count from Redis velocity tracker")
    device_age_days: float = Field(..., description="Device registration age in days")
    account_age_days: float = Field(..., description="Account longevity in days")
    merchant_risk_score: float = Field(..., description="Merchant risk category score (0.0 to 1.0)")
    beneficiary_history_count: float = Field(..., description="Prior successful transactions to target beneficiary")
    behavioral_deviation: float = Field(..., description="Time of day / location behavioral anomaly score")
    graph_cluster_density: float = Field(..., description="Graph relationship cluster density score")

class PredictionResponse(BaseModel):
    fraudProbability: float
    modelVersion: str
    confidence: float
    featureContributions: Dict[str, float]
    datasetLabel: str

class ContinuousTrainRequest(BaseModel):
    labeledSamples: List[Dict[str, Any]] = Field(default=[], description="List of investigator labeled samples (CONFIRMED_FRAUD/FALSE_POSITIVE)")

class ApproveModelRequest(BaseModel):
    modelVersion: str
    approvedBy: str = "HUMAN_ANALYST"

@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "SentinelX ML Fraud Service",
        "modelVersion": ml_engine.current_model_version
    }

@app.post("/predict", response_model=PredictionResponse)
def predict_fraud(request: FeatureVectorRequest):
    try:
        feature_dict = request.model_dump()
        result = ml_engine.predict(feature_dict)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"ML prediction inference error: {str(e)}")

@app.get("/metrics")
def get_model_metrics():
    return ml_engine.get_metrics()

@app.post("/train")
def train_model():
    metrics = ml_engine.train_model()
    return {
        "message": "Model successfully retrained on synthetic development dataset",
        "metrics": metrics
    }

@app.post("/train/continuous")
def train_continuous(request: ContinuousTrainRequest):
    try:
        run_record = ml_engine.train_continuous(request.labeledSamples)
        return {
            "message": "Continuous learning pipeline completed. Candidate model placed in PENDING_APPROVAL status.",
            "runRecord": run_record
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Continuous training error: {str(e)}")

@app.get("/models/candidates")
def get_candidate_models():
    return ml_engine.tracker.get_candidate_models()

@app.post("/models/approve")
def approve_model(request: ApproveModelRequest):
    try:
        record = ml_engine.approve_and_deploy_model(request.modelVersion, request.approvedBy)
        return {
            "message": f"Candidate model {request.modelVersion} successfully approved and deployed to production.",
            "activeRun": record
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Model approval error: {str(e)}")

@app.get("/models/history")
def get_model_history():
    return ml_engine.tracker.get_history()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
