# Mini Online Banking Platform

A simplified banking-style full-stack platform designed for a team of **4 junior full-stack Java developers** to build in **2 weeks**.

The project demonstrates real-world backend, frontend, DevOps, cloud deployment, observability, caching, and event-driven architecture using a practical but beginner-friendly banking domain.

---

## 1. Project Summary

The **Mini Online Banking Platform** allows customers to register, log in, view their bank accounts, check balances, transfer money between their own accounts, view transaction history, and receive notifications after successful transfers.

Admins can create customer bank accounts and view basic customer/account information.

This project is not intended to be a real production banking system. It is a learning-focused capstone project that gives junior developers hands-on experience with a realistic enterprise-style system.

---

## 2. Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT Authentication
* PostgreSQL
* Redis
* Kafka
* Spring Cloud Gateway
* Spring Boot Actuator
* Micrometer
* Zipkin

### Frontend

* React
* TypeScript
* Vite
* React Router
* Axios
* Tailwind CSS
* React Hook Form
* Zod
* Recharts

### DevOps and Cloud

* Docker
* Docker Compose
* Jenkins
* AWS EC2
* AWS RDS PostgreSQL
* AWS ECR
* AWS S3
* AWS CloudFront optional
* AWS ElastiCache optional
* Terraform
* Prometheus
* Grafana
* Zipkin

---

## 3. Core Features

### Customer Features

* Register a new account
* Log in with email and password
* View profile information
* View checking and savings accounts
* View account balances
* View dashboard account summary
* Transfer money between own accounts
* View transaction history
* View notifications
* Mark notifications as read

### Admin Features

* Log in as admin
* View customers
* Create checking or savings account for a customer
* View accounts
* Lock, activate, or deactivate accounts

### System Features

* JWT-based authentication
* Role-based authorization
* API Gateway routing
* Redis caching for account summary
* Kafka event after successful transfer
* Notification Service consumes Kafka events
* PostgreSQL persistence
* Distributed tracing with Zipkin
* Metrics with Prometheus and Grafana
* Dockerized local environment
* Jenkins CI/CD pipeline
* AWS deployment

---

## 4. High-Level Architecture

```text
React Frontend
    ↓
Spring Cloud API Gateway
    ↓
User Service
Account Service
Transaction Service
Notification Service
    ↓
PostgreSQL per service or per schema
Redis for account summary cache
Kafka for transaction events
Zipkin for distributed tracing
Prometheus + Grafana for monitoring
Docker for containers
Jenkins for CI/CD
AWS for deployment
```

---

## 5. Microservices

### 5.1 API Gateway

The API Gateway is the single entry point for frontend requests.

Responsibilities:

* Route requests to backend services
* Validate JWT tokens
* Handle CORS
* Apply optional Redis-based rate limiting
* Forward authenticated user context to services
* Centralize API access for the frontend

Example routes:

```text
/api/auth/**          → user-service
/api/users/**         → user-service
/api/accounts/**      → account-service
/api/transactions/**  → transaction-service
/api/notifications/** → notification-service
```

---

### 5.2 User Service

Handles users, authentication, and roles.

Responsibilities:

* Register customers
* Authenticate users
* Generate JWT tokens
* Store user profile information
* Manage customer/admin roles

Main tables:

* users
* roles
* user_roles

Main APIs:

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
GET  /api/users
```

---

### 5.3 Account Service

Handles customer bank accounts and account balances.

Responsibilities:

* Create checking/savings accounts
* View customer accounts
* View account details
* Return account summary
* Cache account summary in Redis
* Update account balances during transfers

Main tables:

* accounts
* account_audit_logs optional

Main APIs:

```http
POST  /api/accounts
GET   /api/accounts/my
GET   /api/accounts/{accountId}
GET   /api/accounts/summary
GET   /api/accounts
PATCH /api/accounts/{accountId}/status
```

---

### 5.4 Transaction Service

Handles money movement and transaction history.

Responsibilities:

* Transfer money between customer-owned accounts
* Validate transfer rules
* Save transaction records
* Save debit and credit entries
* Publish Kafka transfer events
* Return transaction history

Main tables:

* transactions
* transaction_entries

Main APIs:

```http
POST /api/transactions/transfer
GET  /api/transactions/my
GET  /api/transactions/account/{accountId}
GET  /api/transactions/{transactionId}
```

---

### 5.5 Notification Service

Handles customer notifications.

Responsibilities:

* Listen to Kafka transaction events
* Create customer notifications
* Store notification records
* Return unread/read notifications
* Mark notifications as read

Main tables:

* notifications
* notification_delivery_logs optional

Main APIs:

```http
GET   /api/notifications/my
PATCH /api/notifications/{notificationId}/read
```

---

## 6. Repository Structure

Recommended structure:

```text
mini-banking-platform/
 ┣ frontend/
 ┣ api-gateway/
 ┣ user-service/
 ┣ account-service/
 ┣ transaction-service/
 ┣ notification-service/
 ┣ devops/
 ┃ ┣ prometheus/
 ┃ ┣ grafana/
 ┃ ┗ docker/
 ┣ infra/
 ┣ docker-compose.yml
 ┣ Jenkinsfile
 ┗ README.md
```

---

## 7. Backend Project Structure

Each Spring Boot service should follow a clean layered structure.

```text
com.bank.accountservice
 ┣ controller
 ┣ service
 ┣ repository
 ┣ entity
 ┣ dto
 ┣ mapper
 ┣ exception
 ┣ config
 ┣ client
 ┗ cache
```

### Layer Responsibilities

#### Controller Layer

* Handles HTTP requests
* Validates request input
* Calls service layer
* Returns response DTOs

#### Service Layer

* Contains business logic
* Validates business rules
* Calls repositories
* Calls other services when needed
* Publishes Kafka events when needed

#### Repository Layer

* Handles database access using Spring Data JPA

#### DTO Layer

* Defines request and response objects
* Prevents exposing database entities directly

#### Exception Layer

* Contains custom exceptions
* Uses `@RestControllerAdvice` for consistent error handling

---

## 8. Frontend Structure

Recommended frontend folder structure:

```text
frontend/src/
 ┣ api/
 ┣ components/
 ┣ features/
 ┃ ┣ auth/
 ┃ ┣ dashboard/
 ┃ ┣ accounts/
 ┃ ┣ transactions/
 ┃ ┗ notifications/
 ┣ layouts/
 ┣ routes/
 ┣ types/
 ┗ utils/
```

### Main Frontend Pages

* Login Page
* Register Page
* Customer Dashboard
* Accounts Page
* Transfer Page
* Transaction History Page
* Notifications Page
* Admin Dashboard
* Admin Create Account Page

### Frontend Responsibilities

* Display user-friendly banking dashboard
* Validate forms using React Hook Form and Zod
* Call backend APIs through the API Gateway
* Store JWT token after login
* Attach JWT token to secured requests
* Show loading, success, and error states
* Display account and transaction data

---

## 9. Database Design

Recommended database: **PostgreSQL**

Each service should own its own schema or database.

```text
user-service          → user_db
account-service       → account_db
transaction-service   → transaction_db
notification-service  → notification_db
```

For a junior project, all schemas can run inside one PostgreSQL instance locally.

---

## 10. Main Database Tables

### User Service Tables

#### users

Stores customer and admin profile data.

Fields:

```text
id UUID PRIMARY KEY
first_name VARCHAR(100)
last_name VARCHAR(100)
email VARCHAR(150) UNIQUE
password_hash VARCHAR(255)
phone_number VARCHAR(20)
status VARCHAR(30)
created_at TIMESTAMP
updated_at TIMESTAMP
```

#### roles

Stores roles such as CUSTOMER and ADMIN.

Fields:

```text
id UUID PRIMARY KEY
name VARCHAR(50) UNIQUE
description VARCHAR(255)
```

#### user_roles

Join table between users and roles.

Fields:

```text
user_id UUID
role_id UUID
PRIMARY KEY (user_id, role_id)
```

Relationship:

```text
users many-to-many roles
```

---

### Account Service Tables

#### accounts

Stores customer bank accounts.

Fields:

```text
id UUID PRIMARY KEY
customer_id UUID
account_number VARCHAR(30) UNIQUE
account_type VARCHAR(30)
balance NUMERIC(15,2)
status VARCHAR(30)
created_at TIMESTAMP
updated_at TIMESTAMP
```

Relationship:

```text
customer_id logically references users.id
one customer can have many accounts
```

#### account_audit_logs optional

Tracks changes to account state or balance.

Fields:

```text
id UUID PRIMARY KEY
account_id UUID
action VARCHAR(50)
old_value VARCHAR(255)
new_value VARCHAR(255)
changed_by UUID
reason VARCHAR(255)
created_at TIMESTAMP
```

Relationship:

```text
accounts one-to-many account_audit_logs
```

---

### Transaction Service Tables

#### transactions

Stores the high-level transaction record.

Fields:

```text
id UUID PRIMARY KEY
customer_id UUID
transaction_reference VARCHAR(50) UNIQUE
transaction_type VARCHAR(30)
status VARCHAR(30)
amount NUMERIC(15,2)
description VARCHAR(255)
failure_reason VARCHAR(255)
created_at TIMESTAMP
updated_at TIMESTAMP
```

#### transaction_entries

Stores debit and credit entries for each transaction.

Fields:

```text
id UUID PRIMARY KEY
transaction_id UUID
account_id UUID
entry_type VARCHAR(20)
amount NUMERIC(15,2)
balance_after NUMERIC(15,2)
created_at TIMESTAMP
```

Relationship:

```text
transactions one-to-many transaction_entries
```

A transfer creates one transaction and two entries:

```text
TRANSFER transaction
 ├── DEBIT entry from source account
 └── CREDIT entry to destination account
```

---

### Notification Service Tables

#### notifications

Stores customer notifications.

Fields:

```text
id UUID PRIMARY KEY
customer_id UUID
event_id UUID
notification_type VARCHAR(50)
title VARCHAR(150)
message VARCHAR(500)
status VARCHAR(30)
created_at TIMESTAMP
read_at TIMESTAMP
```

#### notification_delivery_logs optional

Stores delivery attempts for email, SMS, or in-app notification.

Fields:

```text
id UUID PRIMARY KEY
notification_id UUID
channel VARCHAR(30)
delivery_status VARCHAR(30)
attempted_at TIMESTAMP
error_message VARCHAR(255)
```

Relationship:

```text
notifications one-to-many notification_delivery_logs
```

---

## 11. Redis Usage

Redis is used for distributed caching and optional rate limiting.

### Account Summary Cache

Cache key:

```text
customer:{customerId}:account-summary
```

Cached data:

```json
{
  "customerId": "usr_1001",
  "totalBalance": 2000.00,
  "accounts": [
    {
      "accountId": "acc_2001",
      "accountType": "CHECKING",
      "balance": 500.00
    },
    {
      "accountId": "acc_2002",
      "accountType": "SAVINGS",
      "balance": 1500.00
    }
  ]
}
```

Cache rules:

* Read dashboard summary from Redis first
* If cache miss, load from PostgreSQL
* Store result in Redis with TTL
* Invalidate cache after successful transfer

---

## 12. Kafka Messaging

Kafka is used for asynchronous communication between Transaction Service and Notification Service.

Topic:

```text
transaction-events
```

Event example:

```json
{
  "eventId": "evt_9001",
  "eventType": "TRANSFER_COMPLETED",
  "transactionId": "txn_3001",
  "customerId": "usr_1001",
  "fromAccountId": "acc_2001",
  "toAccountId": "acc_2002",
  "amount": 100.00,
  "timestamp": "2026-05-25T10:50:00Z"
}
```

Flow:

```text
Transaction Service
    ↓ publishes event
Kafka topic: transaction-events
    ↓ consumed by
Notification Service
    ↓ creates notification
Customer sees notification in UI
```

---

## 13. Main REST APIs

Base URL:

```http
http://localhost:8080/api
```

Secured endpoints require:

```http
Authorization: Bearer <jwt_token>
```

### Authentication APIs

```http
POST /auth/register
POST /auth/login
GET  /users/me
```

### Account APIs

```http
POST  /accounts
GET   /accounts/my
GET   /accounts/{accountId}
GET   /accounts/summary
GET   /accounts
PATCH /accounts/{accountId}/status
```

### Transaction APIs

```http
POST /transactions/transfer
GET  /transactions/my
GET  /transactions/account/{accountId}
GET  /transactions/{transactionId}
```

### Notification APIs

```http
GET   /notifications/my
PATCH /notifications/{notificationId}/read
```

---

## 14. Example API Payloads

### Register Customer

```http
POST /api/auth/register
```

Request:

```json
{
  "firstName": "Devin",
  "lastName": "Catuns",
  "email": "devin@example.com",
  "password": "Password123!",
  "phoneNumber": "555-123-4567"
}
```

Response:

```json
{
  "userId": "usr_1001",
  "firstName": "Devin",
  "lastName": "Catuns",
  "email": "devin@example.com",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "createdAt": "2026-05-25T10:30:00Z"
}
```

---

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "devin@example.com",
  "password": "Password123!"
}
```

Response:

```json
{
  "accessToken": "jwt-token-value",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": "usr_1001",
    "email": "devin@example.com",
    "role": "CUSTOMER"
  }
}
```

---

### Create Account

```http
POST /api/accounts
```

Request:

```json
{
  "customerId": "usr_1001",
  "accountType": "CHECKING",
  "initialBalance": 500.00
}
```

Response:

```json
{
  "accountId": "acc_2001",
  "customerId": "usr_1001",
  "accountNumber": "1000002345",
  "accountType": "CHECKING",
  "balance": 500.00,
  "status": "ACTIVE",
  "createdAt": "2026-05-25T10:40:00Z"
}
```

---

### Transfer Money

```http
POST /api/transactions/transfer
```

Request:

```json
{
  "fromAccountId": "acc_2001",
  "toAccountId": "acc_2002",
  "amount": 100.00,
  "description": "Move money to savings"
}
```

Response:

```json
{
  "transactionId": "txn_3001",
  "customerId": "usr_1001",
  "fromAccountId": "acc_2001",
  "toAccountId": "acc_2002",
  "amount": 100.00,
  "status": "COMPLETED",
  "message": "Transfer completed successfully",
  "createdAt": "2026-05-25T10:50:00Z"
}
```

---

## 15. Standard Error Response

All services should return a consistent error format.

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Amount must be greater than 0",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T10:50:00Z"
}
```

Common status codes:

| Status | Meaning                        |
| -----: | ------------------------------ |
|    200 | Request successful             |
|    201 | Resource created               |
|    400 | Invalid request                |
|    401 | Missing or invalid token       |
|    403 | User does not have permission  |
|    404 | Resource not found             |
|    409 | Duplicate resource or conflict |
|    500 | Unexpected server error        |

---

## 16. Main Business Rules

### Transfer Rules

* Amount must be greater than zero
* Source account and destination account cannot be the same
* Both accounts must exist
* Both accounts must belong to the logged-in customer
* Both accounts must be ACTIVE
* Source account must have enough balance
* Transaction must be recorded
* Successful transfer must publish Kafka event
* Account summary cache must be invalidated after transfer

---

## 17. Docker Setup

Each backend service should have a Dockerfile.

Example:

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

Frontend Dockerfile:

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

---

## 18. Local Development with Docker Compose

The root `docker-compose.yml` should run:

```text
frontend
api-gateway
user-service
account-service
transaction-service
notification-service
postgres
redis
kafka
zookeeper
zipkin
prometheus
grafana
```

Useful local URLs:

```text
Frontend:    http://localhost:5173
API Gateway: http://localhost:8080
Zipkin:      http://localhost:9411
Prometheus:  http://localhost:9090
Grafana:     http://localhost:3000
PostgreSQL:  localhost:5432
Redis:       localhost:6379
Kafka:       localhost:9092
```

---

## 19. CI/CD Pipeline

Jenkins pipeline stages:

```text
1. Checkout code
2. Run backend tests
3. Run frontend tests
4. Build Spring Boot JARs
5. Build React app
6. Build Docker images
7. Login to Amazon ECR
8. Push images to ECR
9. Deploy to AWS EC2
10. Run smoke tests
```

Smoke test examples:

```http
GET /actuator/health
POST /api/auth/login
GET /api/accounts/summary
```

---

## 20. AWS Deployment

Recommended MVP deployment:

```text
Frontend → S3
Backend services → EC2 with Docker Compose
Docker images → ECR
Database → RDS PostgreSQL
Cache → Redis container locally or ElastiCache
Kafka → Docker container locally or Amazon MSK later
Logs and infrastructure metrics → CloudWatch
```

Recommended AWS services:

| Purpose                | AWS Service          |
| ---------------------- | -------------------- |
| Backend containers     | EC2                  |
| Docker registry        | ECR                  |
| Database               | RDS PostgreSQL       |
| Frontend hosting       | S3                   |
| CDN                    | CloudFront optional  |
| Cache                  | ElastiCache optional |
| Kafka                  | MSK optional         |
| Logs and metrics       | CloudWatch           |
| Infrastructure as code | Terraform            |

---

## 21. Terraform Scope

Terraform can provision:

```text
VPC
Subnets
Security groups
EC2 instance
RDS PostgreSQL
ECR repositories
S3 frontend bucket
IAM roles
```

Suggested structure:

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
   ┣ s3-frontend/
   ┗ security-groups/
```

---

## 22. Observability and Monitoring

### Zipkin

Used for distributed tracing.

Example trace:

```text
React Frontend
 → API Gateway
 → Transaction Service
 → Account Service
 → Kafka
 → Notification Service
```

### Prometheus

Scrapes service metrics from:

```http
/actuator/prometheus
```

### Grafana

Dashboard ideas:

* Request count by service
* Average response time
* Error count
* JVM memory usage
* JVM thread count
* Transfer count
* Failed transfer count
* Kafka event consumption
* Redis cache activity

### Spring Boot Actuator

Each service should expose:

```http
/actuator/health
/actuator/metrics
/actuator/prometheus
```

---

## 23. Testing Strategy

### Backend Testing

Recommended tools:

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc
* Testcontainers optional

Minimum tests per service:

```text
5 tests per service
1 happy path test per main feature
1 failure test per main feature
```

Examples:

User Service:

* Register user successfully
* Reject duplicate email
* Login successfully
* Reject invalid password

Account Service:

* Create account
* Reject negative balance
* Get customer accounts
* Get account summary

Transaction Service:

* Successful transfer
* Reject insufficient balance
* Reject same-account transfer
* Save transaction entries
* Publish Kafka event

Notification Service:

* Consume transfer event
* Create notification
* Get customer notifications
* Mark notification as read

---

### Frontend Testing

Recommended tools:

* Vitest
* React Testing Library

Minimum tests:

* Login form validation
* Register form validation
* Transfer form validation
* Dashboard renders account summary
* Transaction table renders rows

---

### Manual End-to-End Checklist

Use this checklist for final demo testing:

```text
1. Register customer
2. Login as customer
3. Login as admin
4. Admin creates checking account
5. Admin creates savings account
6. Customer views dashboard
7. Customer transfers money
8. Customer views updated balances
9. Customer views transaction history
10. Customer sees notification
11. Customer marks notification as read
12. Zipkin shows request trace
13. Grafana shows service metrics
14. Jenkins pipeline runs successfully
15. App is deployed on AWS
```

---

## 24. Two-Week Implementation Roadmap

### Team Assignments

#### Developer 1 — User/Auth + Login UI

Owns:

* user-service
* JWT authentication
* Login/register UI
* Profile page

#### Developer 2 — Account Service + Dashboard UI

Owns:

* account-service
* Account creation
* Account summary cache
* Dashboard page
* Accounts page

#### Developer 3 — Transaction Service + Transfer UI

Owns:

* transaction-service
* Transfer workflow
* Kafka producer
* Transaction history page
* Transfer page

#### Developer 4 — Gateway, Notification, DevOps

Owns:

* api-gateway
* notification-service
* Kafka consumer
* Docker Compose
* Jenkins
* AWS deployment
* Monitoring setup

---

### Roadmap Summary

| Day | Focus                     | Deliverable                          |
| --: | ------------------------- | ------------------------------------ |
|   1 | Repo, frameworks, tooling | Project skeleton running             |
|   2 | Auth + API Gateway        | Register/login through gateway       |
|   3 | Account Service           | Account APIs and dashboard summary   |
|   4 | Transaction Service       | Transfer and history APIs            |
|   5 | Kafka + Notification      | Transfer event creates notification  |
|   6 | Frontend integration      | UI connected to backend              |
|   7 | Validation and polish     | Clean errors and polished screens    |
|   8 | Testing                   | Backend tests and E2E checklist      |
|   9 | Docker + Jenkins          | Compose and CI/CD working            |
|  10 | AWS + final demo          | Cloud deployment and monitoring demo |

---

## 25. Milestones

### Milestone 1 — Project Foundation

Complete when:

```text
All services are created
Frontend is created
Docker Compose infrastructure runs
Gateway routes are configured
```

### Milestone 2 — Authentication

Complete when:

```text
Customer can register
Customer can log in
JWT is issued
Frontend stores token
Protected routes work
```

### Milestone 3 — Account Management

Complete when:

```text
Admin can create account
Customer can view own accounts
Dashboard summary works
Redis cache is used for summary
```

### Milestone 4 — Money Transfer

Complete when:

```text
Customer can transfer between own accounts
Balances update correctly
Transaction history is saved
Invalid transfers are rejected
```

### Milestone 5 — Kafka Notifications

Complete when:

```text
Transaction Service publishes transfer event
Notification Service consumes event
Customer notification is created
Frontend displays notification
```

### Milestone 6 — DevOps and Deployment

Complete when:

```text
Docker Compose runs full system
Jenkins builds and tests project
Images are pushed to ECR
Application runs on AWS
Health checks and monitoring work
```

---

## 26. MVP Scope

### Must Have

* Register/login
* JWT security
* Customer dashboard
* View accounts
* Admin creates account
* Transfer between own accounts
* View transaction history
* Kafka notification event
* Notification list
* Redis account summary cache
* Docker Compose
* Basic Jenkins pipeline
* Zipkin tracing
* Grafana dashboard

### Should Have

* Admin dashboard
* Rate limiting
* Terraform EC2/RDS/ECR/S3 setup
* CloudWatch logs
* More complete automated tests

### Could Have

* Email integration
* PDF statements
* Account locking workflow
* Fraud detection
* Multi-currency support
* External transfers
* ECS Fargate deployment
* Amazon MSK
* ElastiCache Redis

---

## 27. Features to Avoid for MVP

Avoid these during the first 2 weeks:

* Real payment processing
* External bank transfers
* Credit card processing
* Real KYC verification
* Complex fraud detection
* Kubernetes deployment
* Advanced blue/green deployments
* Real email/SMS provider integration
* Mobile app

The goal is to finish a clean, working platform instead of starting too many features.

---

## 28. Final Demo Script

Recommended final presentation flow:

```text
1. Explain architecture diagram
2. Show React frontend
3. Register a customer
4. Login as admin
5. Create checking account
6. Create savings account
7. Login as customer
8. View dashboard
9. Transfer money between accounts
10. View updated balances
11. View transaction history
12. Show Kafka-created notification
13. Show Zipkin trace
14. Show Grafana metrics dashboard
15. Show Jenkins pipeline
16. Show AWS deployment
```

---

## 29. Project Learning Outcomes

By completing this project, developers will practice:

* Building Spring Boot microservices
* Securing APIs with JWT
* Designing REST APIs
* Modeling normalized relational databases
* Using PostgreSQL with JPA
* Using Redis for distributed caching
* Publishing and consuming Kafka events
* Building React frontend pages
* Integrating frontend and backend through an API Gateway
* Using Docker Compose for local development
* Creating Jenkins CI/CD pipelines
* Deploying to AWS
* Monitoring services with Prometheus, Grafana, and Zipkin
* Working as a team with clear service ownership

---

## 30. Recommended Final Architecture

```text
React Frontend
    ↓
Spring Cloud API Gateway
    ↓
User Service
Account Service
Transaction Service
Notification Service
    ↓
PostgreSQL
Redis
Kafka
    ↓
Zipkin
Prometheus
Grafana
    ↓
Docker
Jenkins
AWS
Terraform
```
