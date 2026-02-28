# SmartCareServer (Microservices Backend) 🚑💻

SmartCareServer is a microservices-based backend for the **Smart Patient Monitoring System**.  
It uses **Spring Boot + Eureka + API Gateway**, messaging via **Kafka**, databases (**MySQL + PostgreSQL**), and observability with **Prometheus + Grafana** — all containerized using **Docker Compose**.

---

## 📌 Services & Ports

| Service | Purpose | Port |
|--------|---------|------|
| discoveryserver | Eureka Service Discovery | `8761` |
| apigateway | API Gateway | `8088` |
| mainservice | Main backend service (MySQL) | `8080` |
| iot-service | IoT/Sensor service (PostgreSQL) | `8082` |
| chatbotbackend | Chatbot backend | `8081` |
| vitalreports | AI Vital Reports service | `8083` |
| mysql | MySQL Database | `3306` |
| postgres | PostgreSQL Database | `5432` |
| zookeeper | Kafka dependency | `2181` |
| kafka | Message broker | `9092` |
| prometheus | Metrics | `9090` |
| grafana | Dashboards | `3001` (maps to 3000) |

---

## ✅ Prerequisites

Install these before running:

- **Java 17**
- **Maven**
- **Docker Desktop** (Linux containers enabled)
- **Git** (optional)

---

## 📦 If you downloaded as a ZIP (recommended steps)

### 1) Extract the ZIP
Extract the project folder anywhere (example):
D:\SmartCareServer\


### 2) Open a terminal in the project root
Make sure you are in the folder that contains:
- `docker-compose.yml`
- `.env`
- service folders like `mainservice/`, `apigateway/`, etc.

---

## 🔐 Environment Variables (.env)

Create a file named **`.env`** in the root (or edit the existing one) with values like:

```env
# MySQL
MYSQL_DATABASE=smartcare_db
MYSQL_ROOT_PASSWORD=your_mysql_password

# PostgreSQL
POSTGRES_DB=sensordb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_postgres_password

# JWT / Secrets
JWT_SECRET=your_jwt_secret

# Chatbot (example)
GEMINI_API_KEY=your_api_key
⚠️ Do not commit real secrets publicly.

🛠 Build JARs first (important)
Because Dockerfiles copy built JARs from target/, you must build first:

mvn clean package -DskipTests
✅ After this, each service should have:

service-name/target/*.jar
🐳 Run with Docker Compose
Start everything
docker compose up --build
Run in background
docker compose up -d --build
Stop everything
docker compose down
Stop + remove volumes (deletes DB data)
docker compose down -v
🌐 Access URLs
Eureka Dashboard:
http://localhost:8761

API Gateway:
http://localhost:8088

Prometheus:
http://localhost:9090

Grafana:
http://localhost:3001
Default login: admin / admin (Grafana may ask to change password)

📊 Monitoring (Prometheus + Grafana)
Prometheus scrape config
Located at:

prometheus/prometheus.yml
Spring Boot Actuator endpoint
Each service exposes:

/actuator/prometheus
Example:

http://localhost:8080/actuator/prometheus

Ensure actuator dependency is included and:
management.endpoints.web.exposure.include=health,info,prometheus

🧪 Troubleshooting
1) "Port already in use"
Stop apps using ports, or change port mappings in docker-compose.yml.

2) "unexpected EOF" / download issues pulling images
Your internet may have interrupted Docker pulls.
Try pulling images one by one:

docker pull mysql:8.0
docker pull postgres:16
docker pull prom/prometheus:v2.54.1
docker pull grafana/grafana:11.1.4
docker pull confluentinc/cp-zookeeper:7.5.3
docker pull confluentinc/cp-kafka:7.5.3
3) Containers start but services can’t connect to DB
Make sure your service uses container hostnames:

MySQL hostname: mysql

Postgres hostname: postgres

Kafka hostname: kafka

Eureka hostname: discoveryserver

📌 Project Structure (example)
SmartCareServer/
├─ docker-compose.yml
├─ .env
├─ apigateway/
├─ discoveryserver/
├─ mainservice/
├─ IOT_service/
├─ chatbotbackend/
├─ vitalReports-AI/
└─ prometheus/
   └─ prometheus.yml
👩‍💻 Author
SmartCareServer - Smart Patient Monitoring System
Developed as a microservices-based university project.
