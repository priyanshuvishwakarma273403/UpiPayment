"""
===============================================================================
SentinelX ML Fraud Detection — Dataset Generator
===============================================================================
DATA SOURCE LABEL: SYNTHETIC DEVELOPMENT DATA
Note: Synthetic data generated for local development and testing purposes.
This dataset is NOT genuine labeled banking data.
===============================================================================
"""

import numpy as np
import pandas as pd

DATASET_LABEL = "SYNTHETIC DEVELOPMENT DATA"

def generate_synthetic_sentinelx_dataset(num_samples: int = 2500, random_seed: int = 42) -> pd.DataFrame:
    """
    Generates synthetic feature vectors matching the SentinelX transaction & entity schema.
    """
    np.random.seed(random_seed)

    # 1. Feature generation
    amount_deviation = np.random.exponential(scale=1.5, size=num_samples) + 0.2
    velocity_1m = np.random.poisson(lam=1.5, size=num_samples)
    device_age_days = np.random.exponential(scale=120, size=num_samples)
    account_age_days = np.random.exponential(scale=365, size=num_samples) + 1
    merchant_risk_score = np.random.uniform(0.0, 1.0, size=num_samples)
    beneficiary_history_count = np.random.poisson(lam=3.0, size=num_samples)
    behavioral_deviation = np.random.beta(a=0.5, b=2.0, size=num_samples)
    graph_cluster_density = np.random.beta(a=0.5, b=3.0, size=num_samples)

    # 2. Synthetic target generation logic (realistic non-linear fraud scoring rule)
    fraud_logits = (
        0.8 * (amount_deviation > 5.0) +
        1.2 * (velocity_1m > 5) +
        1.5 * (device_age_days < 2) +
        0.9 * (account_age_days < 7) +
        0.7 * (merchant_risk_score > 0.7) -
        1.0 * (beneficiary_history_count > 3) +
        1.1 * (behavioral_deviation > 0.7) +
        1.4 * (graph_cluster_density > 0.6) - 2.5
    )

    fraud_probs = 1.0 / (1.0 + np.exp(-fraud_logits))
    is_fraud = (fraud_probs > np.percentile(fraud_probs, 85)).astype(int)

    df = pd.DataFrame({
        "amount_deviation": np.round(amount_deviation, 4),
        "velocity_1m": velocity_1m,
        "device_age_days": np.round(device_age_days, 1),
        "account_age_days": np.round(account_age_days, 1),
        "merchant_risk_score": np.round(merchant_risk_score, 4),
        "beneficiary_history_count": beneficiary_history_count,
        "behavioral_deviation": np.round(behavioral_deviation, 4),
        "graph_cluster_density": np.round(graph_cluster_density, 4),
        "is_fraud": is_fraud
    })

    return df

if __name__ == "__main__":
    df = generate_synthetic_sentinelx_dataset(100)
    print(f"Generated dataset [{DATASET_LABEL}] shape: {df.shape}")
    print(f"Fraud distribution:\n{df['is_fraud'].value_counts()}")
