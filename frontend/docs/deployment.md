# SentinelX — Production Deployment & CI/CD Guide

## Overview
Instructions for deploying the SentinelX Next.js frontend in production.

## Docker Build & Containerization
```bash
docker build -t sentinelx-frontend:latest .
docker run -d -p 3000:3000 -e NEXT_PUBLIC_API_BASE_URL=http://gateway:8080 sentinelx-frontend:latest
```

## Docker Compose
```bash
docker-compose up -d --build
```

## Environment Variables
- `NEXT_PUBLIC_API_BASE_URL`: Base API Gateway URL (Default: `http://localhost:8080`)
- `NODE_ENV`: `production`
