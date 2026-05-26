# Project 1 — 2-Week Implementation Roadmap

## Mini Online Banking Portal

Team size: **4 junior full-stack Java developers**
Timeline: **2 weeks**
Stack: **Java, Spring Boot, React, PostgreSQL, Redis, Kafka, Jenkins, AWS, Docker, Zipkin, Grafana, Terraform**

---

# Team Role Breakdown

## Developer 1 — User/Auth + Login UI

Owns:

```text
user-service
JWT authentication
User registration/login
React login/register pages
User profile page
```

---

## Developer 2 — Account Service + Dashboard UI

Owns:

```text
account-service
Account creation
View customer accounts
Account summary cache with Redis
Customer dashboard
Accounts page
```

---

## Developer 3 — Transaction Service + Transfer UI

Owns:

```text
transaction-service
Transfer workflow
Transaction history
Kafka producer
Transfer page
Transaction history page
```

---

## Developer 4 — Gateway, Notification, DevOps

Owns:

```text
api-gateway
notification-service
Kafka consumer
Docker Compose
Jenkins pipeline
AWS deployment
Zipkin/Grafana/Prometheus
```

---

# Week 1 — Foundation and Core Backend

## Day 1 — Initial Setup

### Goals

Set up the project structure, tooling, shared standards, and local development environment.

---

## Tasks for Everyone

Create GitHub repository:

```text
mini-banking-platform
```

Recommended repo structure:

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

## Backend Setup

Each Spring Boot service should include:

```text
Spring Web
Spring Data JPA
PostgreSQL Driver
Spring Security
Validation
Lombok
Spring Boot Actuator
Micrometer Prometheus
Micrometer Tracing
OpenFeign or WebClient
```

Services:

```text
api-gateway
user-service
account-service
transaction-service
notification-service
```

---

## Frontend Setup

Use:

```text
React
TypeScript
Vite
React Router
Axios
Tailwind CSS
React Hook Form
Zod
Recharts
```

Suggested frontend folders:

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

---

## DevOps Setup

Developer 4 creates:

```text
docker-compose.yml
Dockerfile template
Prometheus config
Grafana setup folder
basic Jenkinsfile
```

Local infrastructure containers:

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

## Day 1 Deliverables

By end of Day 1:

```text
Repo created
All services generated
Frontend app running
Docker Compose infrastructure running
Basic README written
Each developer has assigned service/page ownership
```

---

# Day 2 — User/Auth Service and Gateway Foundation

## Developer 1: User Service

Implement:

```text
User entity
Role entity
UserRole mapping
UserRepository
AuthController
UserController
Register API
Login API
JWT generation
Password hashing with BCrypt
```

Endpoints:

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
```

---

## Developer 4: API Gateway

Implement:

```text
Spring Cloud Gateway
Routes to backend services
CORS configuration
JWT filter
Basic error handling
```

Gateway routes:

```text
/api/auth/**          → user-service
/api/users/**         → user-service
/api/accounts/**      → account-service
/api/transactions/**  → transaction-service
/api/notifications/** → notification-service
```

---

## Frontend Work

Developer 1 starts:

```text
Login page
Register page
Auth API client
JWT storage
Protected route wrapper
```

---

## Day 2 Deliverables

```text
Customer can register
Customer can log in
JWT token returned
Gateway routes auth requests
Frontend login/register pages started
```

---

# Day 3 — Account Service

## Developer 2: Account Service

Implement:

```text
Account entity
AccountRepository
AccountService
AccountController
AdminAccountController
Account DTOs
Account validation
```

Core APIs:

```http
POST /api/accounts
GET  /api/accounts/my
GET  /api/accounts/{accountId}
GET  /api/accounts/summary
```

---

## Redis Integration

Add Redis for account summary cache.

Cache key:

```text
customer:{customerId}:account-summary
```

Rules:

```text
Read account summary from Redis if available
If cache miss, load from PostgreSQL
After transfer, invalidate summary cache
```

---

## Frontend Work

Developer 2 starts:

```text
Dashboard page
Accounts page
Account summary card
Account list table
```

---

## Day 3 Deliverables

```text
Admin can create account
Customer can view own accounts
Customer dashboard can display account summary
Redis cache wired into account summary endpoint
```

---

# Day 4 — Transaction Service

## Developer 3: Transaction Service

Implement:

```text
Transaction entity
TransactionEntry entity
TransactionRepository
TransactionEntryRepository
TransactionService
TransactionController
Transfer validation
```

Core APIs:

```http
POST /api/transactions/transfer
GET  /api/transactions/my
GET  /api/transactions/account/{accountId}
GET  /api/transactions/{transactionId}
```

---

## Transfer Business Rules

Implement:

```text
Amount must be greater than zero
From account cannot equal to account
Both accounts must exist
Both accounts must belong to logged-in customer
Both accounts must be ACTIVE
Source account must have enough balance
```

---

## Service Communication

Transaction Service calls Account Service to:

```text
Validate account ownership
Validate account status
Update balances
Invalidate Redis cache
```

Use:

```text
OpenFeign or WebClient
```

For junior developers, **OpenFeign** is easier.

---

## Frontend Work

Developer 3 starts:

```text
Transfer page
Transaction history page
Transaction API client
```

---

## Day 4 Deliverables

```text
Transfer API works
Transaction records are saved
Transaction entries are saved
Transaction history endpoint works
Transfer UI started
```

---

# Day 5 — Kafka and Notification Service

## Developer 3: Kafka Producer

Transaction Service publishes event after successful transfer.

Topic:

```text
transaction-events
```

Sample event:

```json
{
  "eventId": "evt_9001",
  "eventType": "TRANSFER_COMPLETED",
  "transactionId": "txn_3001",
  "customerId": "usr_1001",
  "amount": 100.00,
  "timestamp": "2026-05-25T10:50:00Z"
}
```

---

## Developer 4: Notification Service

Implement:

```text
Notification entity
NotificationRepository
Kafka consumer
NotificationService
NotificationController
```

Core APIs:

```http
GET   /api/notifications/my
PATCH /api/notifications/{notificationId}/read
```

Kafka behavior:

```text
Consume transaction-events
Create notification record
Mark notification as UNREAD
```

---

## Frontend Work

Developer 4 or Developer 2 adds:

```text
Notification dropdown
Notification list
Mark as read action
```

---

## Day 5 Deliverables

```text
Transaction event is published to Kafka
Notification Service consumes event
Customer notification is created
Frontend can display notifications
```

---

# Week 1 End Milestone

By the end of Week 1, the team should have:

```text
Auth working
Gateway routing working
Account creation working
Account dashboard working
Transfer API working
Transaction history working
Kafka event publishing working
Notification consumer working
Local Docker Compose infrastructure working
```

At this point, the project should already be demoable through Postman and partially demoable through React.

---

# Week 2 — Integration, Testing, DevOps, Deployment

# Day 6 — Frontend and Backend Integration

## Main Goal

Connect all React screens to backend APIs through the API Gateway.

---

## Frontend Integration Tasks

Implement shared Axios client:

```text
Base URL: http://localhost:8080/api
Attach JWT token automatically
Handle 401 responses
Handle API errors globally
```

Pages to complete:

```text
Login
Register
Dashboard
Accounts
Transfer
Transactions
Notifications
Admin account creation
```

---

## Backend Integration Tasks

Fix:

```text
CORS issues
JWT claims between gateway and services
Role-based authorization
Service-to-service URL configs
Common error response format
```

---

## Day 6 Deliverables

```text
Frontend can log in
Frontend can view dashboard
Frontend can view accounts
Frontend can perform transfer
Frontend can view transaction history
Frontend can view notifications
```

---

# Day 7 — Validation, Error Handling, and Polish

## Backend Tasks

Add validation:

```text
@NotBlank
@NotNull
@Email
@Positive
@Size
```

Add global exception handling:

```text
@RestControllerAdvice
```

Standard error response:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Amount must be greater than 0",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T10:50:00Z"
}
```

---

## Frontend Tasks

Add:

```text
Loading states
Error messages
Success messages
Form validation
Empty states
Basic responsive layout
```

---

## Day 7 Deliverables

```text
Transfer form validates correctly
Errors display clearly in UI
Backend returns consistent error responses
Pages look presentable for demo
```

---

# Day 8 — Testing Strategy

## Backend Testing

Each service should include:

```text
Unit tests
Controller tests
Repository tests where useful
Integration tests for core flows
```

Recommended tools:

```text
JUnit 5
Mockito
Spring Boot Test
MockMvc
Testcontainers optional
```

---

## User Service Tests

Test:

```text
Register user successfully
Reject duplicate email
Login successfully
Reject invalid password
Get current user profile
```

---

## Account Service Tests

Test:

```text
Create account
Reject negative initial balance
Get accounts by customer
Get account summary
Redis cache behavior if simple enough
```

---

## Transaction Service Tests

Test:

```text
Successful transfer
Reject insufficient balance
Reject same-account transfer
Reject inactive account
Save transaction entries
Publish Kafka event
```

---

## Notification Service Tests

Test:

```text
Consume transfer event
Create notification
Get customer notifications
Mark notification as read
```

---

## Frontend Testing

Keep it lightweight.

Test:

```text
Login form validation
Transfer form validation
Dashboard renders account summary
Transaction table renders rows
```

Recommended tools:

```text
Vitest
React Testing Library
```

---

## Manual End-to-End Test Checklist

Use this as a team demo checklist:

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
```

---

## Day 8 Deliverables

```text
Core backend unit tests written
Main frontend form tests written
Manual E2E checklist passing
Bugs from integration fixed
```

---

# Day 9 — Docker and CI/CD

## Docker Tasks

Each service should have:

```text
Dockerfile
application-docker.yml
Health check endpoint
Environment variable configuration
```

Root project should have:

```text
docker-compose.yml
```

Docker Compose should run:

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

---

## Jenkins Pipeline Tasks

Create Jenkins stages:

```text
Checkout
Backend tests
Frontend tests
Build backend JARs
Build frontend
Build Docker images
Push images to ECR
Deploy to EC2
Smoke test
```

---

## Smoke Tests

After deployment, run:

```text
GET /actuator/health
POST /api/auth/login with test user
GET /api/accounts/summary
```

---

## Day 9 Deliverables

```text
docker compose up --build works
All services communicate in Docker network
Jenkinsfile created
Jenkins can run tests and build images
Basic smoke test works
```

---

# Day 10 — AWS Deployment and Final Demo

## AWS Deployment Tasks

Use simple AWS setup:

```text
EC2 for backend containers
RDS PostgreSQL
ECR for Docker images
S3 for React frontend
CloudFront optional
ElastiCache Redis optional
Kafka can remain Docker-based for MVP
```

For 2 weeks, the practical deployment target is:

```text
Frontend → S3
Backend → EC2 Docker Compose
Database → RDS PostgreSQL
Images → ECR
```

---

## Terraform Tasks

Create basic Terraform for:

```text
VPC
Security group
EC2 instance
RDS PostgreSQL
ECR repositories
S3 frontend bucket
IAM role
```

Terraform can be basic. The goal is to show infrastructure-as-code, not perfect production infrastructure.

---

## Monitoring Setup

Verify:

```text
Zipkin receives traces
Prometheus scrapes services
Grafana dashboard displays metrics
Spring Boot Actuator health works
Application logs are visible
```

Grafana starter panels:

```text
Request count
Response time
Error count
JVM memory
Transfer count
Failed transfers
```

---

## Final Demo Flow

Demo this workflow:

```text
1. Open React frontend
2. Register customer
3. Login as admin
4. Create checking account
5. Create savings account
6. Login as customer
7. View dashboard
8. Transfer money
9. View transaction history
10. Show notification created from Kafka event
11. Show Zipkin trace
12. Show Grafana dashboard
13. Show Jenkins pipeline
14. Show AWS deployment
```

---

# Full 2-Week Roadmap Summary

| Day | Main Focus                 | Main Deliverable                     |
| --: | -------------------------- | ------------------------------------ |
|   1 | Repo, tools, service setup | Project skeleton running             |
|   2 | Auth + Gateway             | Register/login through gateway       |
|   3 | Account Service            | Accounts and dashboard API           |
|   4 | Transaction Service        | Transfer and history APIs            |
|   5 | Kafka + Notification       | Transfer event creates notification  |
|   6 | Frontend integration       | UI connected to backend              |
|   7 | Validation and polish      | Clean errors and polished screens    |
|   8 | Testing                    | Core tests and E2E checklist         |
|   9 | Docker + Jenkins           | Compose and CI/CD working            |
|  10 | AWS + final demo           | Cloud deployment and monitoring demo |

---

# Core Feature Milestones

## Milestone 1 — Project Foundation

Complete when:

```text
All services are created
Frontend is created
Docker Compose infrastructure runs
Gateway routes are configured
```

---

## Milestone 2 — Authentication

Complete when:

```text
Customer can register
Customer can log in
JWT is issued
Frontend stores token
Protected routes work
```

---

## Milestone 3 — Account Management

Complete when:

```text
Admin can create account
Customer can view own accounts
Dashboard summary works
Redis cache is used for summary
```

---

## Milestone 4 — Money Transfer

Complete when:

```text
Customer can transfer between own accounts
Balances update correctly
Transaction history is saved
Invalid transfers are rejected
```

---

## Milestone 5 — Kafka Notifications

Complete when:

```text
Transaction Service publishes transfer event
Notification Service consumes event
Customer notification is created
Frontend displays notification
```

---

## Milestone 6 — DevOps and Deployment

Complete when:

```text
Docker Compose runs full system
Jenkins builds and tests project
Images are pushed to ECR
Application runs on AWS
Health checks and monitoring work
```

---

# Testing Strategy Summary

## Backend

Minimum testing target:

```text
5 tests per service
At least 1 happy path test per main feature
At least 1 failure test per main feature
```

Recommended backend coverage:

```text
Auth: register/login
Account: create/view/summary
Transaction: successful transfer/failed transfer
Notification: consume event/create notification
Gateway: secured route behavior
```

---

## Frontend

Minimum testing target:

```text
Login form
Register form
Transfer form
Dashboard render
Transaction table render
```

---

## Integration Testing

Use a manual E2E checklist first.

Optional advanced testing:

```text
Testcontainers for PostgreSQL
Testcontainers for Kafka
Cypress or Playwright for frontend E2E
```

For junior developers, manual E2E plus backend unit tests is enough for the 2-week scope.

---

# Deployment Checklist

Before deploying:

```text
All services build locally
All tests pass
Docker Compose works
Environment variables are externalized
JWT secret is not hardcoded
Database URL points to RDS
Docker images are pushed to ECR
EC2 security group allows required ports only
Frontend API URL points to deployed gateway
Health checks pass
```

---

# Recommended Final Scope

For this project, the team should not try to build every banking feature.

## Build These

```text
Register/login
Admin creates customer accounts
Customer views accounts
Customer views account summary
Customer transfers money between own accounts
Customer views transaction history
Kafka notification after transfer
Redis account summary cache
Docker Compose
Jenkins pipeline
AWS EC2 deployment
Zipkin/Grafana monitoring
```

## Avoid These for MVP

```text
External bank transfers
Real payment processing
Credit cards
Loan applications
Multi-currency
Complex fraud detection
Real email/SMS integration
Mobile app
Advanced Kubernetes deployment
```

The goal is a clean, working banking-style platform that demonstrates full-stack development, microservices, messaging, caching, CI/CD, cloud deployment, and monitoring.
