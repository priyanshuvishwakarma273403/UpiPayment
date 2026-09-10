# SentinelX / UPI Mesh — Local Development Runbook

This guide provides step-by-step instructions for setting up, running, monitoring, and troubleshooting the complete **SentinelX / UPI Mesh** platform locally using Docker Compose.

---

## 1. Prerequisites

Before starting, ensure the following software is installed on your workstation:

- **Docker Desktop** (v24.0+ with Docker Compose v2.20+)
- **Git**
- *(Optional for local IDE development)*: **JDK 17** & **Maven 3.9+**

---

## 2. Environment Configuration

The repository uses environment variables centralized in `.env`.

1. Copy the provided `.env.example` template to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Verify default settings inside `.env`:
   - `MYSQL_USER`: `upimesh`
   - `MYSQL_PASSWORD`: `upimesh_secret`
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: `http://admin:admin123@eureka-server:8761/eureka/`
   - `JWT_SECRET`: Pre-configured 256-bit development key

---

## 3. Starting the Platform

To build images and launch all infrastructure, core platform services, and 24 domain microservices in detached mode:

```bash
docker compose up -d
```

### Starting Infrastructure First (Recommended for slower machines):

If you prefer starting infrastructure services before microservices:

```bash
# Step 1: Start Databases & Message Brokers
docker compose up -d mysql redis mongodb zookeeper kafka

# Step 2: Verify infrastructure health
docker compose ps

# Step 3: Start Service Discovery & API Gateway
docker compose up -d eureka-server api-gateway

# Step 4: Start remaining domain microservices
docker compose up -d
```

---

## 4. Health Checks & Verification

### Container Status
Check all running containers and their health status:
```bash
docker compose ps
```

### Eureka Discovery Dashboard
Open your browser and navigate to:
```
http://localhost:8761
```
Login credentials: `admin` / `admin123`. All active microservices will automatically register with Eureka.

### API Gateway Health
Check the central gateway health:
```bash
curl http://localhost:8080/actuator/health
```

---

## 5. Log Inspection & Monitoring

- **View logs for all services:**
  ```bash
  docker compose logs -f --tail=50
  ```
- **View logs for a specific service (e.g., Payment or NPCI):**
  ```bash
  docker compose logs -f payment
  docker compose logs -f npci
  ```
- **View infrastructure logs:**
  ```bash
  docker compose logs -f mysql
  docker compose logs -f kafka
  ```

---

## 6. Rebuilding & Modifying Microservices

If you modify code in a microservice (e.g., `PaymentService.java`):

1. Rebuild only that specific microservice container:
   ```bash
   docker compose build payment
   ```
2. Restart the container without disturbing other running services:
   ```bash
   docker compose up -d --no-deps payment
   ```

---

## 7. Stopping & Resetting the Platform

- **Stop all services (preserving data volumes):**
  ```bash
  docker compose down
  ```
- **Stop all services and purge database volumes (Fresh Start):**
  ```bash
  docker compose down -v
  ```

---

## 8. Ports Reference Table

| Service / Infrastructure | Container Name | Port Mapping | Health / Access Endpoint |
| :--- | :--- | :--- | :--- |
| **MySQL Database** | `upimesh-mysql` | `3306:3306` | `mysqladmin ping` |
| **Redis Cache** | `upimesh-redis` | `6379:6379` | `redis-cli ping` |
| **MongoDB** | `upimesh-mongo` | `27017:27017` | `mongosh --eval 'db.adminCommand("ping")'` |
| **Zookeeper** | `upimesh-zookeeper` | `2181:2181` | TCP 2181 |
| **Apache Kafka** | `upimesh-kafka` | `9092:9092` | PLAINTEXT://kafka:9092 |
| **Eureka Discovery** | `upimesh-eureka` | `8761:8761` | `http://localhost:8761` |
| **API Gateway** | `upimesh-gateway` | `8080:8080` | `http://localhost:8080/actuator/health` |
| **Auth Service** | `upimesh-auth` | `8081:8081` | `/auth/health` |
| **Wallet Service** | `upimesh-wallet` | `8082:8082` | `/wallet/health` |
| **Payment Service** | `upimesh-payment` | `8083:8083` | `/payment/health` |
| **Transaction Service** | `upimesh-transaction` | `8084:8084` | `/transactions/health` |
| **Merchant Service** | `upimesh-merchant` | `8085:8085` | `/merchant/health` |
| **Fraud Service** | `upimesh-fraud` | `8086:8086` | `/fraud/health` |
| **AI Service** | `upimesh-aiservice` | `8087:8087` | `/ai/health` |
| **Notification Service**| `upimesh-notification` | `8088:8088` | `/notification/health` |
| **Sync Service** | `upimesh-sync` | `8089:8089` | `/sync/health` |
| **NPCI Service** | `upimesh-npci` | `8090:8090` | `/npci/health` |
| **Bank Gateway** | `upimesh-bankgateway` | `8091:8091` | `/bankgateway/health` |
| **Settlement Service** | `upimesh-settlement` | `8092:8092` | `/settlement/health` |
| **Reconciliation Service**| `upimesh-reconciliation` | `8093:8093` | `/reconciliation/health` |
| **KYC Service** | `upimesh-kyc` | `8094:8094` | `/kyc/health` |
| **AML Service** | `upimesh-aml` | `8095:8095` | `/aml/health` |
| **Risk Service** | `upimesh-risk` | `8096:8096` | `/risk/health` |
| **Subscription Service** | `upimesh-subscription` | `8098:8098` | `/subscription/health` |
| **Invoice Service** | `upimesh-invoice` | `8099:8099` | `/invoice/health` |
| **Dispute Service** | `upimesh-dispute` | `8100:8100` | `/dispute/health` |
| **Rewards Service** | `upimesh-rewards` | `8101:8101` | `/rewards/health` |
| **Loan Service** | `upimesh-loan` | `8102:8102` | `/loan/health` |
| **Referral Service** | `upimesh-referral` | `8103:8103` | `/referral/health` |
| **Payroll Service** | `upimesh-payroll` | `8104:8104` | `/payroll/health` |
| **Analytics Service** | `upimesh-analytics` | `8105:8105` | `/analytics/health` |

---

## 9. Troubleshooting

### Port Already in Use
If port `3306`, `6379`, or `8080` is already in use by a local service:
1. Stop local MySQL / Redis instances running on host machine.
2. Or override ports in `.env` (e.g. `MYSQL_PORT=3307`).

### Database Connection Refused on Startup
Services use Docker Compose `depends_on` with `condition: service_healthy` for MySQL. If a service fails to connect on startup, ensure MySQL container has finished executing `docker/mysql/init.sql`.

### Docker Daemon Not Running
If you receive `failed to connect to the docker API`:
1. Start Docker Desktop application on your machine.
2. Verify Docker CLI responsiveness with `docker info`.
