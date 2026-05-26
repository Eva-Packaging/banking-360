# Project 1: Mini Online Banking Portal — System Design

## 1. Project Overview

The **Mini Online Banking Portal** is a simplified banking platform where customers can log in, view their accounts, check balances, see transaction history, and transfer money between their own accounts.

The goal is not to build a real bank-grade system, but to give junior developers hands-on practice with:

* Spring Boot microservices
* React frontend
* PostgreSQL database
* Redis distributed caching
* Kafka event-driven messaging
* API Gateway
* Jenkins CI/CD
* Docker deployment
* AWS infrastructure
* Zipkin distributed tracing
* Grafana monitoring

---

# 2. Core Features

## Customer Features

Customers should be able to:

* Register and log in
* View their profile
* View bank accounts
* View account balance
* View transaction history
* Transfer money between their own accounts
* See transfer success or failure messages

## Admin Features

Admins should be able to:

* View customers
* Create bank accounts for customers
* View all accounts
* View all transactions
* Monitor basic transaction activity

## System Features

The system should:

* Publish Kafka events when a transfer happens
* Send mock notifications through Notification Service
* Cache account summaries in Redis
* Trace requests using Zipkin
* Expose metrics for Grafana dashboards

---

# 3. Frontend Tech Stack and Responsibilities

## Frontend Tech Stack

Use:

* **React**
* **TypeScript**
* **React Router**
* **Axios**
* **Tailwind CSS**
* **React Hook Form**
* **Zod** for validation
* **Recharts** for dashboard charts
* **JWT-based authentication**

---

## Frontend Responsibilities

The frontend is responsible for the user experience and calling backend APIs through the API Gateway.

## Main Pages

### 1. Login Page

Allows customers and admins to log in.

Fields:

* Email
* Password

Responsibilities:

* Validate input
* Call Auth API
* Store JWT token
* Redirect user based on role

---

### 2. Register Page

Allows a new customer to register.

Fields:

* First name
* Last name
* Email
* Password
* Phone number

Responsibilities:

* Validate form
* Call User Service through gateway
* Show success/error messages

---

### 3. Customer Dashboard

Shows customer account summary.

Displays:

* Customer name
* Total balance
* Checking account
* Savings account
* Recent transactions

Responsibilities:

* Call Account Service API
* Display cached account summary
* Show loading/error states

---

### 4. Accounts Page

Shows all accounts owned by the logged-in customer.

Displays:

* Account number
* Account type
* Balance
* Status

Account types:

* CHECKING
* SAVINGS

---

### 5. Transactions Page

Shows transaction history.

Filters:

* Account
* Transaction type
* Date range

Transaction types:

* DEPOSIT
* WITHDRAWAL
* TRANSFER_IN
* TRANSFER_OUT

---

### 6. Transfer Page

Allows a customer to transfer money between their own accounts.

Fields:

* From account
* To account
* Amount
* Description

Responsibilities:

* Validate amount
* Prevent same-account transfer
* Call Transaction Service API
* Show transfer result

---

### 7. Admin Dashboard

Admin can:

* View all customers
* Create accounts for customers
* View system transactions
* View simple activity charts

---

# 4. Backend Microservices Structure

Use a small microservice architecture with 5 services.

## Recommended Services

```text
api-gateway
user-service
account-service
transaction-service
notification-service
```

Optional but useful:

```text
discovery-service
config-service
```

For a 2-week junior project, I would skip config-service unless the team is comfortable.

---

# 5. Microservice Responsibilities

## 1. API Gateway

The API Gateway is the single entry point for frontend requests.

### Responsibilities

* Route requests to backend services
* Validate JWT token
* Handle CORS
* Apply basic rate limiting using Redis
* Add request tracing headers
* Centralize frontend API access

### Technology

* Spring Cloud Gateway
* Spring Security
* Redis rate limiter
* Zipkin tracing

### Example Routes

```text
/api/auth/**          -> user-service
/api/users/**         -> user-service
/api/accounts/**      -> account-service
/api/transactions/**  -> transaction-service
/api/notifications/** -> notification-service
```

---

## 2. User Service

Handles users, authentication, and roles.

### Responsibilities

* Register customer
* Login user
* Generate JWT token
* Store customer profile
* Manage user roles

### Main Entities

```text
User
Role
```

### Roles

```text
CUSTOMER
ADMIN
```

### Layers

```text
controller
service
repository
dto
entity
mapper
exception
security
```

### Controllers

```text
AuthController
UserController
```

### Example APIs

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
GET  /api/users
```

---

## 3. Account Service

Handles customer bank accounts and account balances.

### Responsibilities

* Create account for customer
* View customer accounts
* View account details
* Update account balance after transfer
* Cache account summaries in Redis

### Main Entities

```text
Account
AccountType
AccountStatus
```

### Account Types

```text
CHECKING
SAVINGS
```

### Account Statuses

```text
ACTIVE
INACTIVE
LOCKED
```

### Layers

```text
controller
service
repository
dto
entity
mapper
exception
client
cache
```

### Controllers

```text
AccountController
AdminAccountController
```

### Example APIs

```http
POST /api/accounts
GET  /api/accounts/customer/{customerId}
GET  /api/accounts/{accountId}
GET  /api/accounts/summary/{customerId}
PATCH /api/accounts/{accountId}/balance
```

---

## 4. Transaction Service

Handles transfers and transaction history.

### Responsibilities

* Transfer money between accounts
* Validate balance
* Record transaction history
* Publish Kafka event after successful transfer
* Retrieve transaction history

### Main Entities

```text
Transaction
TransactionType
TransactionStatus
```

### Transaction Types

```text
TRANSFER_IN
TRANSFER_OUT
DEPOSIT
WITHDRAWAL
```

### Transaction Statuses

```text
PENDING
COMPLETED
FAILED
```

### Layers

```text
controller
service
repository
dto
entity
mapper
exception
producer
client
```

### Controllers

```text
TransactionController
```

### Example APIs

```http
POST /api/transactions/transfer
GET  /api/transactions/account/{accountId}
GET  /api/transactions/customer/{customerId}
GET  /api/transactions/{transactionId}
```

---

## 5. Notification Service

Handles mock notifications after banking events.

### Responsibilities

* Listen to Kafka transaction events
* Create notification records
* Simulate email/SMS notification
* Allow customer to view notifications

### Main Entities

```text
Notification
NotificationType
NotificationStatus
```

### Notification Types

```text
TRANSFER_SUCCESS
TRANSFER_FAILED
ACCOUNT_CREATED
```

### Layers

```text
consumer
controller
service
repository
dto
entity
exception
```

### Example APIs

```http
GET /api/notifications/customer/{customerId}
PATCH /api/notifications/{notificationId}/read
```

---

# 6. Backend Layered Architecture

Each Spring Boot service should follow a clean layered structure.

## Standard Package Structure

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

---

## Controller Layer

Handles HTTP requests.

Responsibilities:

* Receive API request
* Validate request body
* Call service layer
* Return response DTO

Example:

```text
TransferController receives transfer request
↓
calls TransactionService
↓
returns transfer result
```

---

## Service Layer

Contains business logic.

Responsibilities:

* Validate business rules
* Call repositories
* Call other services if needed
* Publish Kafka events
* Manage transactions

Example business rules:

* Transfer amount must be greater than zero
* From-account and to-account cannot be the same
* Source account must have enough balance
* Account must be active

---

## Repository Layer

Handles database access.

Responsibilities:

* Use Spring Data JPA
* Query PostgreSQL tables
* Return entity objects

---

## DTO Layer

Used to avoid exposing database entities directly.

Common DTOs:

```text
LoginRequest
LoginResponse
RegisterRequest
AccountResponse
TransferRequest
TransactionResponse
NotificationResponse
```

---

## Exception Layer

Each service should have custom exceptions and global exception handling.

Examples:

```text
UserNotFoundException
AccountNotFoundException
InsufficientBalanceException
InvalidTransferException
TransactionFailedException
```

Use:

```text
@RestControllerAdvice
```

for consistent error responses.

---

# 7. Database Choice and Justification

## Database: PostgreSQL

Use **PostgreSQL** as the main database.

## Why PostgreSQL?

PostgreSQL is a strong choice for this banking-style project because:

* It supports reliable relational data modeling
* It has strong transaction support
* It supports foreign keys and constraints
* It is good for financial-style records
* It works well with Spring Data JPA
* It is easy to run locally with Docker
* It is available on AWS using RDS

For a banking platform, relational consistency matters. Account balances, users, and transaction history should be stored in a structured SQL database.

---

# 8. Database Per Service

For microservices, each service should own its own database schema.

Simple setup:

```text
bank_user_db
bank_account_db
bank_transaction_db
bank_notification_db
```

For local development, all schemas can exist inside one PostgreSQL container.

For production-style AWS deployment, they can be separate databases or schemas in Amazon RDS PostgreSQL.

---

## User Service Tables

```text
users
roles
user_roles
```

### users

```text
id
first_name
last_name
email
password_hash
phone_number
status
created_at
updated_at
```

---

## Account Service Tables

```text
accounts
```

### accounts

```text
id
customer_id
account_number
account_type
balance
status
created_at
updated_at
```

Important:

`customer_id` references the user ID logically, but not with a direct foreign key across services.

---

## Transaction Service Tables

```text
transactions
```

### transactions

```text
id
customer_id
from_account_id
to_account_id
amount
transaction_type
status
description
created_at
updated_at
```

---

## Notification Service Tables

```text
notifications
```

### notifications

```text
id
customer_id
type
message
status
created_at
read_at
```

---

# 9. Redis Usage

Redis should be used in a simple, meaningful way.

## Recommended Redis Use Cases

### 1. Account Summary Cache

Cache customer account summary:

```text
customer:{customerId}:account-summary
```

Example cached data:

```json
{
  "customerId": "123",
  "totalBalance": 2500.75,
  "accounts": [
    {
      "accountId": "acc-1",
      "type": "CHECKING",
      "balance": 1200.50
    },
    {
      "accountId": "acc-2",
      "type": "SAVINGS",
      "balance": 1300.25
    }
  ]
}
```

### 2. Rate Limiting at API Gateway

Limit login or transfer requests:

```text
10 requests per minute per user/IP
```

### 3. Token Blacklist Optional

If logout is implemented, store invalidated JWT tokens temporarily.

---

# 10. Kafka Messaging Setup

Kafka should be used for asynchronous events.

## Main Kafka Topic

```text
transaction-events
```

## Event Example

```json
{
  "eventId": "evt-1001",
  "eventType": "TRANSFER_COMPLETED",
  "customerId": "user-123",
  "fromAccountId": "acc-1",
  "toAccountId": "acc-2",
  "amount": 150.00,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

## Kafka Flow

```text
Transaction Service
    ↓ publishes event
Kafka topic: transaction-events
    ↓ consumed by
Notification Service
    ↓ creates notification
Customer sees notification in UI
```

This keeps notification processing separate from the money transfer flow.

---

# 11. API Gateway and Microservices Setup

## API Gateway Responsibilities

The frontend should never call microservices directly.

All frontend requests go through:

```text
React App → API Gateway → Microservices
```

## Example Request Flow

### Login Flow

```text
React Login Page
    ↓
API Gateway
    ↓
User Service
    ↓
PostgreSQL user database
    ↓
JWT returned to frontend
```

---

### Account Summary Flow

```text
React Dashboard
    ↓
API Gateway
    ↓
Account Service
    ↓
Check Redis cache
    ↓ if cache miss
PostgreSQL account database
    ↓
Return account summary
```

---

### Transfer Flow

```text
React Transfer Page
    ↓
API Gateway
    ↓
Transaction Service
    ↓
Transaction Service validates request
    ↓
Transaction Service calls Account Service
    ↓
Account Service updates balances
    ↓
Transaction Service saves transaction record
    ↓
Transaction Service publishes Kafka event
    ↓
Notification Service consumes event
    ↓
Notification saved
```

---

# 12. Important Business Rules

Keep the rules simple.

## Transfer Rules

* Amount must be greater than zero
* Source account must exist
* Destination account must exist
* Both accounts must belong to the same customer
* Source account must have enough balance
* Both accounts must be active
* Same account transfer is not allowed
* Transaction record must be saved whether successful or failed

---

# 13. Security Design

## Authentication

Use JWT authentication.

### Login Process

```text
User enters email and password
User Service validates credentials
User Service returns JWT
React stores JWT
React sends JWT in Authorization header
API Gateway validates JWT
Request is forwarded to target service
```

Example header:

```http
Authorization: Bearer <token>
```

---

## Authorization

Use role-based access.

### CUSTOMER

Can:

* View own profile
* View own accounts
* View own transactions
* Transfer between own accounts
* View own notifications

### ADMIN

Can:

* View all customers
* Create accounts
* View all accounts
* View all transactions

---

# 14. Observability Design

Use:

* Zipkin
* Grafana
* Prometheus
* Spring Boot Actuator

## Zipkin

Tracks request flow across services.

Example:

```text
Frontend request
→ API Gateway
→ Transaction Service
→ Account Service
→ Kafka event
→ Notification Service
```

This helps students understand distributed tracing.

---

## Grafana

Grafana can show dashboards for:

* API request count
* Error count
* Service response time
* JVM memory usage
* Kafka consumer activity
* Redis cache hit/miss
* PostgreSQL health

---

## Spring Boot Actuator

Each service should expose:

```http
/actuator/health
/actuator/metrics
/actuator/prometheus
```

---

# 15. Deployment Environment

## Local Development

Use Docker Compose.

Services:

```text
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

React can run separately with:

```text
npm run dev
```

or inside Docker.

---

## Cloud Deployment

Use AWS.

## Recommended AWS Services

```text
Amazon EC2 or ECS
Amazon RDS PostgreSQL
Amazon ElastiCache Redis
Amazon MSK or self-hosted Kafka on EC2
Amazon ECR
CloudWatch
S3 for frontend static hosting
```

For junior developers, the simplest AWS setup is:

```text
Frontend: S3 + CloudFront
Backend: EC2 with Docker Compose
Database: RDS PostgreSQL
Redis: ElastiCache
Images: ECR
CI/CD: Jenkins
```

A more advanced setup would be:

```text
Frontend: S3 + CloudFront
Backend: ECS Fargate
Database: RDS PostgreSQL
Redis: ElastiCache
Kafka: MSK
Images: ECR
CI/CD: Jenkins
Infrastructure: Terraform
```

For 2 weeks, I recommend **EC2 + Docker Compose** first, then optionally move to ECS if time allows.

---

# 16. Docker Setup

Each backend service should have its own Dockerfile.

Example services:

```text
api-gateway/Dockerfile
user-service/Dockerfile
account-service/Dockerfile
transaction-service/Dockerfile
notification-service/Dockerfile
```

Root-level:

```text
docker-compose.yml
```

The Docker Compose file should run:

```text
PostgreSQL
Redis
Kafka
Zipkin
Prometheus
Grafana
Backend services
```

---

# 17. Jenkins CI/CD

## Jenkins Pipeline Responsibilities

For each push to main branch:

```text
Checkout code
Run backend tests
Build Spring Boot services
Build Docker images
Push images to ECR
Deploy to EC2 or ECS
```

## Simple Jenkins Pipeline Flow

```text
GitHub
  ↓
Jenkins
  ↓
Run Tests
  ↓
Build JARs
  ↓
Build Docker Images
  ↓
Push to AWS ECR
  ↓
Deploy using Docker Compose on EC2
```

---

# 18. Terraform Responsibilities

Terraform should provision the AWS infrastructure.

## Terraform Can Create

```text
VPC
Subnets
Security groups
EC2 instance
RDS PostgreSQL
ElastiCache Redis
ECR repositories
S3 bucket
CloudFront distribution
IAM roles
```

For junior developers, start with:

```text
EC2
RDS
ECR
S3
Security groups
```

Then add Redis and CloudFront if time allows.

---

# 19. Component Interaction Diagram

```text
                 ┌────────────────────┐
                 │    React Frontend   │
                 │  S3 / CloudFront    │
                 └─────────┬──────────┘
                           │
                           ▼
                 ┌────────────────────┐
                 │    API Gateway      │
                 │ Spring Cloud Gateway│
                 └──────┬─────┬───────┘
                        │     │
        ┌───────────────┘     └───────────────┐
        ▼                                     ▼
┌─────────────────┐                 ┌──────────────────┐
│  User Service   │                 │ Account Service  │
│ Auth + Profile  │                 │ Accounts/Balance │
└───────┬─────────┘                 └───────┬──────────┘
        │                                   │
        ▼                                   ▼
┌─────────────────┐                 ┌──────────────────┐
│ PostgreSQL DB   │                 │ PostgreSQL DB    │
│ user_db         │                 │ account_db       │
└─────────────────┘                 └───────┬──────────┘
                                            │
                                            ▼
                                    ┌──────────────────┐
                                    │ Redis Cache      │
                                    │ Account Summary  │
                                    └──────────────────┘

                 ┌────────────────────────────┐
                 │   Transaction Service       │
                 │ Transfers + History         │
                 └─────────────┬──────────────┘
                               │
                               ▼
                    ┌────────────────────┐
                    │ PostgreSQL DB       │
                    │ transaction_db      │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │ Kafka               │
                    │ transaction-events  │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │ Notification Service│
                    │ Mock Email/SMS      │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │ PostgreSQL DB       │
                    │ notification_db     │
                    └────────────────────┘
```

---

# 20. Suggested Team Breakdown

## Developer 1 — User/Auth Service + Login UI

Responsibilities:

* User Service
* JWT login/register
* Role-based access
* Login/Register pages
* Profile page

---

## Developer 2 — Account Service + Account UI

Responsibilities:

* Account Service
* Create/view accounts
* Account summary cache with Redis
* Dashboard page
* Accounts page

---

## Developer 3 — Transaction Service + Transfer UI

Responsibilities:

* Transaction Service
* Transfer money flow
* Transaction history
* Kafka producer
* Transfer page
* Transactions page

---

## Developer 4 — Gateway, Notification, DevOps

Responsibilities:

* API Gateway
* Notification Service
* Kafka consumer
* Docker Compose
* Jenkins pipeline
* AWS/Terraform basics
* Zipkin/Grafana setup

---

# 21. Recommended MVP Scope

For 2 weeks, keep the MVP focused.

## Must Have

* Login/register
* JWT security
* Customer dashboard
* View accounts
* Create account as admin
* Transfer between own accounts
* View transactions
* Kafka notification event
* Redis cache for account summary
* Docker Compose
* Basic Jenkins pipeline
* Zipkin tracing
* Grafana dashboard

## Should Have

* Admin dashboard
* Rate limiting
* Notification list in UI
* Terraform EC2/RDS setup
* Prometheus metrics

## Could Have

* Email integration
* PDF statements
* Account locking
* Fraud detection
* Multi-currency support
* External transfers

Avoid the “Could Have” features unless the MVP is complete.

---

# 22. Final Recommended Architecture

Use this simplified architecture:

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
PostgreSQL per service
Redis for account summary cache
Kafka for transaction notification events
Zipkin for tracing
Prometheus + Grafana for monitoring
Docker for containers
Jenkins for CI/CD
Terraform for AWS infrastructure
AWS EC2/RDS/S3/ECR for deployment
```