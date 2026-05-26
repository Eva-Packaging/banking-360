# Project 1 — Deployment & DevOps Setup

This setup is designed for the **Mini Online Banking Portal** with:

```text
React
Spring Boot microservices
PostgreSQL
Redis
Kafka
Zipkin
Prometheus
Grafana
Jenkins
Docker
AWS
Terraform
```

For a 2-week junior team, I recommend this rollout:

```text
Phase 1: Run everything locally with Docker Compose
Phase 2: Add Jenkins CI/CD
Phase 3: Deploy to AWS using EC2 + Docker Compose
Phase 4: Optional upgrade to ECS Fargate
```

---

# 1. Deployment Architecture Overview

## Simple MVP Deployment

```text
Developer pushes code to GitHub
        ↓
Jenkins pipeline runs
        ↓
Builds React + Spring Boot services
        ↓
Runs tests
        ↓
Builds Docker images
        ↓
Pushes images to Amazon ECR
        ↓
Deploys containers to AWS EC2
        ↓
App runs behind Nginx or Application Load Balancer
```

AWS is a good fit here because it gives you managed services for compute, containers, database, cache, monitoring, and image storage. Amazon ECR stores Docker images, RDS can host PostgreSQL, and CloudWatch can collect service logs and metrics. AWS documents that many AWS services publish metrics to CloudWatch, which makes it useful for infrastructure-level monitoring. ([AWS Documentation][1])

---

# 2. Services to Containerize

You should create one Docker image per application service.

```text
frontend
api-gateway
user-service
account-service
transaction-service
notification-service
```

Infrastructure containers for local development:

```text
postgres
redis
kafka
zookeeper
zipkin
prometheus
grafana
```

---

# 3. Backend Dockerfile

Each Spring Boot microservice can use the same Dockerfile pattern.

Example path:

```text
user-service/Dockerfile
account-service/Dockerfile
transaction-service/Dockerfile
notification-service/Dockerfile
api-gateway/Dockerfile
```

## Spring Boot Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Notes

Each service should expose a different internal port locally:

```text
api-gateway: 8080
user-service: 8081
account-service: 8082
transaction-service: 8083
notification-service: 8084
```

Inside Docker, the services can still expose `8080`, but Docker Compose maps them differently.

---

# 4. Frontend Dockerfile

Example path:

```text
frontend/Dockerfile
```

## React Production Dockerfile

```dockerfile
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

If using Vite React, the production build usually outputs:

```text
dist
```

If using Create React App, it usually outputs:

```text
build
```

---

# 5. Local Docker Compose Setup

Use Docker Compose so all developers can run the same local environment.

Example path:

```text
docker-compose.yml
```

## Docker Compose Example

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:16
    container_name: bank-postgres
    environment:
      POSTGRES_USER: bank_user
      POSTGRES_PASSWORD: bank_password
      POSTGRES_DB: bank_db
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    container_name: bank-redis
    ports:
      - "6379:6379"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.1
    container_name: bank-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.6.1
    container_name: bank-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: bank-zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://bank-kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  zipkin:
    image: openzipkin/zipkin
    container_name: bank-zipkin
    ports:
      - "9411:9411"

  prometheus:
    image: prom/prometheus
    container_name: bank-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./devops/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    container_name: bank-grafana
    ports:
      - "3000:3000"

  api-gateway:
    build: ./api-gateway
    container_name: bank-api-gateway
    depends_on:
      - user-service
      - account-service
      - transaction-service
      - notification-service
      - redis
      - zipkin
    ports:
      - "8080:8080"
    environment:
      USER_SERVICE_URL: http://user-service:8080
      ACCOUNT_SERVICE_URL: http://account-service:8080
      TRANSACTION_SERVICE_URL: http://transaction-service:8080
      NOTIFICATION_SERVICE_URL: http://notification-service:8080
      REDIS_HOST: redis
      ZIPKIN_ENDPOINT: http://zipkin:9411/api/v2/spans

  user-service:
    build: ./user-service
    container_name: bank-user-service
    depends_on:
      - postgres
      - zipkin
    ports:
      - "8081:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bank_db
      SPRING_DATASOURCE_USERNAME: bank_user
      SPRING_DATASOURCE_PASSWORD: bank_password
      ZIPKIN_ENDPOINT: http://zipkin:9411/api/v2/spans

  account-service:
    build: ./account-service
    container_name: bank-account-service
    depends_on:
      - postgres
      - redis
      - zipkin
    ports:
      - "8082:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bank_db
      SPRING_DATASOURCE_USERNAME: bank_user
      SPRING_DATASOURCE_PASSWORD: bank_password
      REDIS_HOST: redis
      ZIPKIN_ENDPOINT: http://zipkin:9411/api/v2/spans

  transaction-service:
    build: ./transaction-service
    container_name: bank-transaction-service
    depends_on:
      - postgres
      - kafka
      - zipkin
    ports:
      - "8083:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bank_db
      SPRING_DATASOURCE_USERNAME: bank_user
      SPRING_DATASOURCE_PASSWORD: bank_password
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      ZIPKIN_ENDPOINT: http://zipkin:9411/api/v2/spans

  notification-service:
    build: ./notification-service
    container_name: bank-notification-service
    depends_on:
      - postgres
      - kafka
      - zipkin
    ports:
      - "8084:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bank_db
      SPRING_DATASOURCE_USERNAME: bank_user
      SPRING_DATASOURCE_PASSWORD: bank_password
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      ZIPKIN_ENDPOINT: http://zipkin:9411/api/v2/spans

  frontend:
    build: ./frontend
    container_name: bank-frontend
    depends_on:
      - api-gateway
    ports:
      - "5173:80"

volumes:
  postgres_data:
```

---

# 6. Prometheus Configuration

Example path:

```text
devops/prometheus/prometheus.yml
```

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "api-gateway"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["api-gateway:8080"]

  - job_name: "user-service"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["user-service:8080"]

  - job_name: "account-service"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["account-service:8080"]

  - job_name: "transaction-service"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["transaction-service:8080"]

  - job_name: "notification-service"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["notification-service:8080"]
```

Each Spring Boot service needs these dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

And this config:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

---

# 7. Spring Boot Observability Config

For tracing with Zipkin, add Micrometer tracing dependencies.

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

Application config:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0

  zipkin:
    tracing:
      endpoint: ${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}
```

For production, reduce sampling:

```yaml
management:
  tracing:
    sampling:
      probability: 0.1
```

---

# 8. CI/CD Pipeline Overview

## Recommended Pipeline Stages

```text
1. Checkout code
2. Run backend unit tests
3. Run frontend tests
4. Build Spring Boot JARs
5. Build React app
6. Build Docker images
7. Login to Amazon ECR
8. Push images to ECR
9. Deploy to AWS
10. Run smoke tests
```

Jenkins supports Docker-based pipeline workflows through plugins, and the Amazon ECR Jenkins plugin integrates ECR authentication as a Docker registry token source. ([Jenkins Plugins][2]) Jenkins also provides Pipeline step references for building scripted or declarative pipelines. ([Jenkins][3])

---

# 9. Jenkinsfile Example

This is a simplified Jenkins pipeline for the full project.

```groovy
pipeline {
    agent any

    environment {
        AWS_REGION = "us-east-1"
        AWS_ACCOUNT_ID = "123456789012"
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

        API_GATEWAY_IMAGE = "${ECR_REGISTRY}/bank-api-gateway"
        USER_SERVICE_IMAGE = "${ECR_REGISTRY}/bank-user-service"
        ACCOUNT_SERVICE_IMAGE = "${ECR_REGISTRY}/bank-account-service"
        TRANSACTION_SERVICE_IMAGE = "${ECR_REGISTRY}/bank-transaction-service"
        NOTIFICATION_SERVICE_IMAGE = "${ECR_REGISTRY}/bank-notification-service"
        FRONTEND_IMAGE = "${ECR_REGISTRY}/bank-frontend"

        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Tests') {
            parallel {
                stage('User Service Tests') {
                    steps {
                        dir('user-service') {
                            sh './mvnw test'
                        }
                    }
                }

                stage('Account Service Tests') {
                    steps {
                        dir('account-service') {
                            sh './mvnw test'
                        }
                    }
                }

                stage('Transaction Service Tests') {
                    steps {
                        dir('transaction-service') {
                            sh './mvnw test'
                        }
                    }
                }

                stage('Notification Service Tests') {
                    steps {
                        dir('notification-service') {
                            sh './mvnw test'
                        }
                    }
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm test -- --watch=false || true'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh """
                    docker build -t ${API_GATEWAY_IMAGE}:${IMAGE_TAG} ./api-gateway
                    docker build -t ${USER_SERVICE_IMAGE}:${IMAGE_TAG} ./user-service
                    docker build -t ${ACCOUNT_SERVICE_IMAGE}:${IMAGE_TAG} ./account-service
                    docker build -t ${TRANSACTION_SERVICE_IMAGE}:${IMAGE_TAG} ./transaction-service
                    docker build -t ${NOTIFICATION_SERVICE_IMAGE}:${IMAGE_TAG} ./notification-service
                    docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ./frontend
                """
            }
        }

        stage('Login to ECR') {
            steps {
                sh """
                    aws ecr get-login-password --region ${AWS_REGION} \
                    | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                """
            }
        }

        stage('Push Docker Images') {
            steps {
                sh """
                    docker push ${API_GATEWAY_IMAGE}:${IMAGE_TAG}
                    docker push ${USER_SERVICE_IMAGE}:${IMAGE_TAG}
                    docker push ${ACCOUNT_SERVICE_IMAGE}:${IMAGE_TAG}
                    docker push ${TRANSACTION_SERVICE_IMAGE}:${IMAGE_TAG}
                    docker push ${NOTIFICATION_SERVICE_IMAGE}:${IMAGE_TAG}
                    docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                """
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['bank-ec2-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ec2-user@<EC2_PUBLIC_IP> '
                            cd /opt/bank-platform &&
                            export IMAGE_TAG=${IMAGE_TAG} &&
                            docker compose pull &&
                            docker compose up -d
                        '
                    """
                }
            }
        }

        stage('Smoke Test') {
            steps {
                sh """
                    curl -f http://<EC2_PUBLIC_IP>:8080/actuator/health
                """
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully.'
        }

        failure {
            echo 'Pipeline failed. Check Jenkins logs.'
        }
    }
}
```

---

# 10. AWS Deployment Option 1: Simple EC2 Deployment

This is the best option for a 2-week student project.

## AWS Services

| Purpose                   | AWS Service                                |
| ------------------------- | ------------------------------------------ |
| Run backend containers    | EC2                                        |
| Store Docker images       | ECR                                        |
| Database                  | RDS PostgreSQL                             |
| Redis cache               | ElastiCache Redis                          |
| Frontend hosting          | S3 + CloudFront                            |
| Logs and infra metrics    | CloudWatch                                 |
| Secrets                   | AWS Secrets Manager or SSM Parameter Store |
| Networking                | VPC, Security Groups                       |
| Infrastructure automation | Terraform                                  |

Jenkins can also be deployed on EC2; Jenkins’ AWS tutorial describes using EC2 to launch Jenkins and optionally add build agents when more build capacity is needed. ([Jenkins][4])

---

## Simple AWS Layout

```text
Internet
   ↓
CloudFront
   ↓
S3 React Frontend
   ↓
API Gateway URL
   ↓
Application Load Balancer or EC2 Public DNS
   ↓
Docker Compose on EC2
   ↓
Spring Boot Microservices
   ↓
RDS PostgreSQL
ElastiCache Redis
Kafka container or MSK
```

---

## EC2 Deployment Pros

Good for junior team because:

```text
Easy to understand
Easy to debug
Works well with Docker Compose
Cheaper and faster to set up
Less AWS complexity
```

## EC2 Deployment Cons

```text
Less production-grade
Manual scaling is harder
One EC2 instance can become a single point of failure
```

---

# 11. AWS Deployment Option 2: ECS Fargate

This is more production-like but more complex.

## AWS Services

| Purpose               | AWS Service                     |
| --------------------- | ------------------------------- |
| Run containers        | ECS Fargate                     |
| Load balancing        | Application Load Balancer       |
| Docker image registry | ECR                             |
| Database              | RDS PostgreSQL                  |
| Cache                 | ElastiCache Redis               |
| Kafka                 | Amazon MSK                      |
| Frontend              | S3 + CloudFront                 |
| Logs                  | CloudWatch Logs                 |
| Metrics               | CloudWatch + Prometheus/Grafana |
| Secrets               | Secrets Manager                 |
| Infrastructure        | Terraform                       |

AWS ECS can route container logs to CloudWatch by configuring the `awslogs` log driver in the task definition. ([AWS Documentation][5])

---

## ECS Architecture

```text
React Frontend
    ↓
S3 + CloudFront
    ↓
Application Load Balancer
    ↓
API Gateway ECS Service
    ↓
Internal ECS Services:
        user-service
        account-service
        transaction-service
        notification-service
    ↓
RDS PostgreSQL
ElastiCache Redis
Amazon MSK Kafka
```

---

# 12. Terraform Setup

Recommended folder structure:

```text
infra/
 ┣ main.tf
 ┣ variables.tf
 ┣ outputs.tf
 ┣ providers.tf
 ┣ environments/
 ┃ ┣ dev.tfvars
 ┃ ┗ prod.tfvars
 ┗ modules/
   ┣ vpc/
   ┣ ec2/
   ┣ rds/
   ┣ ecr/
   ┣ redis/
   ┣ s3-frontend/
   ┣ security-groups/
   ┗ cloudwatch/
```

---

## Terraform Should Create

For the MVP:

```text
VPC
Public subnet
Security groups
EC2 instance
ECR repositories
RDS PostgreSQL
S3 bucket for frontend
IAM role for EC2
```

Optional:

```text
ElastiCache Redis
CloudFront distribution
Application Load Balancer
MSK Kafka
CloudWatch alarms
```

---

# 13. Environment Variables

Each service should read configuration from environment variables.

## Common Variables

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
JWT_SECRET=change-me
ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
```

## Database Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://rds-endpoint:5432/bank_db
SPRING_DATASOURCE_USERNAME=bank_user
SPRING_DATASOURCE_PASSWORD=bank_password
```

## Redis Variables

```env
REDIS_HOST=redis-endpoint
REDIS_PORT=6379
```

## Kafka Variables

```env
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
TRANSACTION_EVENTS_TOPIC=transaction-events
```

## Service URL Variables

```env
USER_SERVICE_URL=http://user-service:8080
ACCOUNT_SERVICE_URL=http://account-service:8080
TRANSACTION_SERVICE_URL=http://transaction-service:8080
NOTIFICATION_SERVICE_URL=http://notification-service:8080
```

---

# 14. Secrets Management

Do not hardcode secrets in code or Dockerfiles.

Sensitive values:

```text
JWT secret
Database password
AWS credentials
Redis password
Kafka credentials
```

For local development:

```text
.env file
Docker Compose environment variables
```

For AWS:

```text
AWS Secrets Manager
SSM Parameter Store
ECS task secrets
EC2 environment file with restricted permissions
```

---

# 15. Scalability Approach

## Frontend Scaling

Use:

```text
S3 + CloudFront
```

Benefits:

```text
Static React files are served globally
No frontend server to manage
CloudFront improves performance
```

---

## Backend Scaling

### EC2 MVP

For the 2-week project:

```text
Run all containers on one EC2 instance
Increase EC2 size if needed
Use Docker Compose restart policies
```

Example:

```yaml
restart: always
```

### Production-Like Scaling

Move to:

```text
ECS Fargate
Application Load Balancer
Multiple replicas per service
Auto Scaling
```

AWS supports ECS services behind load balancers, and ECS services can be scaled based on demand when configured with service auto scaling and CloudWatch metrics. ([AWS Documentation][1])

---

## Database Scaling

Start with:

```text
Amazon RDS PostgreSQL single instance
```

Then improve with:

```text
Multi-AZ for high availability
Read replicas for read-heavy endpoints
Connection pooling with HikariCP
Indexes on customer_id, account_id, created_at
```

---

## Redis Scaling

Use Redis for:

```text
Account summary cache
Rate limiting
Optional token blacklist
```

Production option:

```text
Amazon ElastiCache Redis
```

Scaling strategy:

```text
Set TTL on cached dashboard data
Invalidate cache after transfers
Use Redis for gateway rate limiting
```

Example cache key:

```text
customer:{customerId}:account-summary
```

---

## Kafka Scaling

For local/MVP:

```text
Single Kafka container
```

For production-like AWS:

```text
Amazon MSK
Multiple partitions for transaction-events topic
Consumer groups for notification-service
```

Recommended topic:

```text
transaction-events
```

Partition key:

```text
customerId
```

Using `customerId` as the key keeps customer-related transaction events ordered per customer.

---

# 16. Monitoring Approach

Use two layers of monitoring.

## Application Monitoring

Use:

```text
Spring Boot Actuator
Micrometer
Prometheus
Grafana
Zipkin
```

Track:

```text
HTTP request count
HTTP response time
Error rate
JVM memory
JVM threads
Database connection pool usage
Kafka producer/consumer metrics
Redis cache activity
```

---

## Infrastructure Monitoring

Use:

```text
AWS CloudWatch
```

Track:

```text
EC2 CPU
EC2 memory, if CloudWatch agent is installed
RDS CPU
RDS connections
RDS storage
ElastiCache CPU/memory
ALB request count
ALB 4xx/5xx errors
ECS task health, if using ECS
```

CloudWatch supports AWS service metrics across many services, including API Gateway and other managed services. ([AWS Documentation][1])

---

# 17. Grafana Dashboard Ideas

Create dashboards for junior developers to understand system health.

## Dashboard 1: API Health

Panels:

```text
Request count by service
Average response time
Error count
HTTP 4xx count
HTTP 5xx count
```

## Dashboard 2: JVM Health

Panels:

```text
Heap memory usage
Thread count
Garbage collection time
CPU usage
```

## Dashboard 3: Banking Metrics

Custom business metrics:

```text
Number of transfers
Total transferred amount
Failed transfers
Successful transfers
Active customers
Account summary cache hits
```

## Dashboard 4: Kafka Metrics

Panels:

```text
Events published
Events consumed
Consumer lag
Notification failures
```

---

# 18. Logging Strategy

Use structured logs.

Each service should log:

```text
requestId
traceId
userId if available
serviceName
endpoint
statusCode
durationMs
errorMessage
```

Example log format:

```json
{
  "timestamp": "2026-05-25T12:00:00Z",
  "service": "transaction-service",
  "traceId": "abc-123",
  "userId": "usr_1001",
  "action": "TRANSFER",
  "status": "COMPLETED",
  "durationMs": 220
}
```

For AWS ECS, route logs to CloudWatch Logs using the `awslogs` driver. AWS documents this as the standard task definition log configuration pattern for ECS containers. ([AWS Documentation][5])

---

# 19. Health Checks

Every Spring Boot service should expose:

```http
GET /actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

Docker Compose can include health checks:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

Load balancer health checks should target:

```text
/api-gateway/actuator/health
```

or:

```text
/actuator/health
```

depending on routing.

---

# 20. Deployment Flow for the Team

## Local Developer Flow

```text
1. Developer pulls latest code
2. Runs docker compose up --build
3. Tests frontend at http://localhost:5173
4. Tests API Gateway at http://localhost:8080
5. Opens Zipkin at http://localhost:9411
6. Opens Prometheus at http://localhost:9090
7. Opens Grafana at http://localhost:3000
```

---

## CI/CD Flow

```text
1. Developer creates feature branch
2. Opens pull request
3. Jenkins runs tests
4. Team reviews code
5. Merge to main
6. Jenkins builds Docker images
7. Jenkins pushes images to ECR
8. Jenkins deploys to AWS
9. Jenkins runs smoke test
```

---

# 21. Recommended Branch Strategy

Keep it simple:

```text
main
develop
feature/user-service
feature/account-service
feature/transaction-service
feature/frontend-dashboard
feature/devops
```

Flow:

```text
feature branch → pull request → develop → main
```

For a 2-week project, you can also simplify:

```text
feature branch → pull request → main
```

---

# 22. Minimum DevOps Scope for 2 Weeks

## Must Have

```text
Dockerfile for each backend service
Dockerfile for frontend
docker-compose.yml
PostgreSQL container
Redis container
Kafka container
Zipkin container
Prometheus container
Grafana container
Basic Jenkinsfile
Build and test pipeline
Push Docker images to ECR
Deploy to one EC2 instance
```

## Should Have

```text
Terraform for EC2, RDS, ECR, and S3
CloudWatch logs
Grafana dashboard
Health checks
Smoke tests
```

## Could Have

```text
ECS Fargate deployment
Application Load Balancer
Auto Scaling
Amazon MSK
ElastiCache Redis
RDS Multi-AZ
Blue/green deployment
```

---

# 23. Final Recommended DevOps Design

For this team and timeline, use this:

```text
Local:
Docker Compose runs all services and infrastructure.

CI/CD:
Jenkins runs tests, builds Docker images, pushes to ECR, deploys to EC2.

Cloud:
React hosted on S3 + CloudFront.
Spring Boot services run as Docker containers on EC2.
PostgreSQL runs on RDS.
Redis can start as Docker locally, then move to ElastiCache.
Kafka can start as Docker locally, then optionally move to MSK.
Logs and infrastructure metrics go to CloudWatch.
Application metrics go to Prometheus and Grafana.
Tracing goes to Zipkin.
```

This setup gives the team real DevOps experience without making the deployment too advanced for a 2-week junior-level project.

[1]: https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/aws-services-cloudwatch-metrics.html?utm_source=chatgpt.com "AWS services that publish CloudWatch metrics"
[2]: https://plugins.jenkins.io/amazon-ecr/?utm_source=chatgpt.com "Amazon ECR | Jenkins plugin"
[3]: https://www.jenkins.io/doc/pipeline/steps/?utm_source=chatgpt.com "Pipeline Steps Reference"
[4]: https://www.jenkins.io/doc/tutorials/tutorial-for-installing-jenkins-on-AWS/?utm_source=chatgpt.com "Jenkins on AWS"
[5]: https://docs.aws.amazon.com/AmazonECS/latest/developerguide/specify-log-config.html?utm_source=chatgpt.com "Example Amazon ECS task definition: Route logs to ..."
