# ⚡ BootForge AI — Spring Boot 4 + AI SaaS Starter Kit

**Ship your AI-powered SaaS on Java 21 & Spring Boot 4 in days—not months.**

Stop spending 60+ hours wiring authentication, database migrations, Docker infrastructure, and Spring AI integration from scratch. **BootForge AI** gives you a clean, modular monolith engineered for high throughput with Java 21 Virtual Threads and production-ready defaults.

[![Java 21](https://img.shields.io/badge/Java-21%20(Virtual%20Threads)-orange.svg)](https://openjdk.org/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-blue.svg)](https://spring.io/projects/spring-ai)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ⚖️ Lite (Free) vs. BootForge AI Pro

This open-source repository is the **MIT-licensed Lite Edition**. Building a commercial B2B SaaS? Skip weeks of infrastructure work with **[BootForge AI Pro →](https://polar.sh/your-checkout-link)**

| Feature / Capability | **BootForge AI Lite** *(Free — MIT)* | **BootForge AI Pro** *(One-Time License)* |
| :--- | :---: | :---: |
| **Core Runtime:** Java 21 (Virtual Threads) + Spring Boot 4.1 | ✅ | ✅ |
| **Database & Migrations:** PostgreSQL 17 + Flyway SQL Scripts | ✅ | ✅ |
| **Local Dev Stack:** Docker Compose (Postgres, Redis, Mailpit) | ✅ | ✅ |
| **Authentication:** JWT Access Tokens + SHA-256 Refresh Token Rotation | ✅ | ✅ |
| **Spring AI Chat:** Single-Prompt Endpoint (`POST /api/v1/ai/chat`) | ✅ | ✅ |
| **Global Error Handling:** RFC-7807 `ProblemDetail` Exception Handler | ✅ | ✅ |
| **B2B Multi-Tenancy:** Zero-Leak Workspace Isolation (`X-Tenant-Id` + `TenantFilter`) | — | ✅ |
| **Granular RBAC:** Declarative `@RequiresRole` Guards (`OWNER`, `ADMIN`, `MEMBER`) | — | ✅ |
| **Billing & Subscriptions:** Idempotent Stripe Checkout & Webhook Engine | — | ✅ |
| **Production RAG Pipeline:** `TokenTextSplitter` + `pgvector` Tenant-Filtered Search | — | ✅ |
| **Real-Time Streaming:** Server-Sent Events (SSE) AI Chat Stream | — | ✅ |
| **AI Cost Guardrails:** Redis-Backed Per-Tenant Daily Token Quotas | — | ✅ |
| **Security Test Suite:** Cross-Tenant Isolation Integration Tests | — | ✅ |

👉 **[Upgrade to BootForge AI Pro ($199 Solo / $349 Team) →](https://buy.polar.sh/polar_cl_bEoOSNGbA3IdbuPqmAgEXVBthgrf54WXtLP2e3G9ekc)**

---

## 🚀 How to Run Locally

### 1. Prerequisites
Ensure the following tools are installed and running on your machine:
- **Java 21 JDK** (e.g., Eclipse Temurin 21)
- **Docker & Docker Compose** (Docker Desktop must be running)
- **Maven 3.9+** (or use the included `./mvnw` / `mvnw.cmd` wrapper)

### 2. Clone the Repository & Configure `.env`
```bash
git clone [https://github.com/vigneshg13/bootforge-ai.git](https://github.com/vigneshg13/bootforge-ai.git)
cd bootforge-ai
cp .env.example .env

Open `.env` and add your OpenAI API key to enable the AI chat endpoint:

```env
OPENAI_API_KEY=sk-your-openai-api-key
```

### 3. Start Backing Services (PostgreSQL 17 + pgvector, Redis 7, Mailpit)

```bash
docker compose -f docker/docker-compose.yml up -d
```

Verify that all containers are healthy:

```bash
docker compose -f docker/docker-compose.yml ps
```

### 4. Start the Spring Boot Application

- **macOS / Linux:**
  ```bash
  ./mvnw spring-boot:run
  ```
- **Windows (Command Prompt / PowerShell):**
  ```bash
  mvnw.cmd spring-boot:run
  ```

Once started, the API is available at `http://localhost:8080`:
- **Health Check:** `http://localhost:8080/actuator/health`
- **Local Email Sandbox (Mailpit UI):** `http://localhost:8025`

---

## 🔌 Quick API Smoke Test

### 1. Register a User & Workspace

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "founder@acme.com",
    "password": "Password123!",
    "fullName": "Jane Founder",
    "workspaceName": "Acme Corp"
  }'
```

Copy the `accessToken` from the JSON response.

### 2. Send an Authenticated AI Chat Request

```bash
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Give me 3 tips for scaling a Java 21 B2B SaaS."
  }'
```

---

## 🧪 Running Integration Tests

The test suite uses **Testcontainers** to automatically spin up an isolated `pgvector/pgvector:pg17` Docker container during execution.

1. Confirm **Docker Desktop** is running.
2. Run the test suite:

```bash
# macOS / Linux
./mvnw test

# Windows
mvnw.cmd test
```

---

## 🐳 Building the Production Docker Image

Build the multi-stage Alpine runtime image (using Java 21 + Generational ZGC):

```bash
docker build -f docker/Dockerfile -t bootforge-ai:latest .
docker run -p 8080:8080 --env-file .env bootforge-ai:latest
```

---

## 📂 Project Structure

```text
src/main/java/com/acme/saas/
├── Application.java               # Spring Boot entry point (@EnableAsync)
├── common/exception/              # BusinessException & RFC-7807 GlobalExceptionHandler
├── auth/                          # User, RefreshToken, JwtService, SecurityConfig, AuthController
├── tenant/domain/                 # Organization, Membership, Role, PlanTier entities
└── ai/                            # AiController & RagChatService (Spring AI ChatClient)
```