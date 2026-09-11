# Multi-Stage Build for SentinelX Python ML Microservice
# Stage 1: Build stage
FROM python:3.12-slim AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt || \
    pip install --no-cache-dir --user fastapi uvicorn xgboost scikit-learn numpy pandas pydantic

# Stage 2: Minimal Runtime stage
FROM python:3.12-slim
RUN groupadd -g 10001 appgroup && useradd -u 10001 -g appgroup -s /bin/bash appuser

WORKDIR /app
COPY --from=builder /root/.local /home/appuser/.local
COPY . .

ENV PATH=/home/appuser/.local/bin:$PATH
RUN chown -R appuser:appgroup /app

USER 10001
EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

ENTRYPOINT ["python", "main.py"]
