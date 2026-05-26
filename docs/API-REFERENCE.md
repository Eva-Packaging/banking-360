# Mini Online Banking Portal — REST API Reference

Base URL through API Gateway:

```http
http://localhost:8080/api
```

For secured endpoints, send:

```http
Authorization: Bearer <jwt_token>
```

---

# 1. Authentication APIs

## 1.1 Register Customer

Creates a new customer account.

```http
POST /auth/register
```

### Request Body

```json
{
  "firstName": "Devin",
  "lastName": "Catuns",
  "email": "devin@example.com",
  "password": "Password123!",
  "phoneNumber": "555-123-4567"
}
```

### Success Response — `201 Created`

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

### Possible Errors

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Email already exists",
  "path": "/api/auth/register",
  "timestamp": "2026-05-25T10:30:00Z"
}
```

---

## 1.2 Login

Authenticates a user and returns a JWT token.

```http
POST /auth/login
```

### Request Body

```json
{
  "email": "devin@example.com",
  "password": "Password123!"
}
```

### Success Response — `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": "usr_1001",
    "email": "devin@example.com",
    "role": "CUSTOMER"
  }
}
```

### Possible Errors

```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid email or password",
  "path": "/api/auth/login",
  "timestamp": "2026-05-25T10:32:00Z"
}
```

---

# 2. User APIs

## 2.1 Get Logged-In User Profile

Returns the profile of the currently authenticated user.

```http
GET /users/me
```

### Headers

```http
Authorization: Bearer <jwt_token>
```

### Success Response — `200 OK`

```json
{
  "userId": "usr_1001",
  "firstName": "Devin",
  "lastName": "Catuns",
  "email": "devin@example.com",
  "phoneNumber": "555-123-4567",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "createdAt": "2026-05-25T10:30:00Z"
}
```

### Possible Errors

```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Missing or invalid JWT token",
  "path": "/api/users/me",
  "timestamp": "2026-05-25T10:35:00Z"
}
```

---

## 2.2 Get All Customers

Admin-only endpoint.

```http
GET /users
```

### Query Parameters

| Name   | Required | Example  | Description           |
| ------ | -------: | -------- | --------------------- |
| page   |       No | `0`      | Page number           |
| size   |       No | `10`     | Page size             |
| status |       No | `ACTIVE` | Filter by user status |

### Example Request

```http
GET /api/users?page=0&size=10&status=ACTIVE
```

### Success Response — `200 OK`

```json
{
  "content": [
    {
      "userId": "usr_1001",
      "firstName": "Devin",
      "lastName": "Catuns",
      "email": "devin@example.com",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

### Possible Errors

```json
{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Only admins can view all customers",
  "path": "/api/users",
  "timestamp": "2026-05-25T10:36:00Z"
}
```

---

# 3. Account APIs

## 3.1 Create Customer Account

Admin creates a checking or savings account for a customer.

```http
POST /accounts
```

### Request Body

```json
{
  "customerId": "usr_1001",
  "accountType": "CHECKING",
  "initialBalance": 500.00
}
```

### Success Response — `201 Created`

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

### Possible Errors

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Initial balance cannot be negative",
  "path": "/api/accounts",
  "timestamp": "2026-05-25T10:41:00Z"
}
```

---

## 3.2 Get My Accounts

Returns all accounts for the logged-in customer.

```http
GET /accounts/my
```

### Success Response — `200 OK`

```json
[
  {
    "accountId": "acc_2001",
    "accountNumber": "1000002345",
    "accountType": "CHECKING",
    "balance": 500.00,
    "status": "ACTIVE"
  },
  {
    "accountId": "acc_2002",
    "accountNumber": "1000009876",
    "accountType": "SAVINGS",
    "balance": 1500.00,
    "status": "ACTIVE"
  }
]
```

### Possible Errors

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "No accounts found for customer",
  "path": "/api/accounts/my",
  "timestamp": "2026-05-25T10:42:00Z"
}
```

---

## 3.3 Get Account by ID

Returns one account by account ID.

```http
GET /accounts/{accountId}
```

### Path Parameters

| Name      | Required | Example    |
| --------- | -------: | ---------- |
| accountId |      Yes | `acc_2001` |

### Success Response — `200 OK`

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

### Possible Errors

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Account not found",
  "path": "/api/accounts/acc_9999",
  "timestamp": "2026-05-25T10:43:00Z"
}
```

---

## 3.4 Get Customer Account Summary

Used by the dashboard. This endpoint can use Redis caching.

```http
GET /accounts/summary
```

### Success Response — `200 OK`

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
  ],
  "cached": true
}
```

### Possible Errors

```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Authentication required",
  "path": "/api/accounts/summary",
  "timestamp": "2026-05-25T10:44:00Z"
}
```

---

# 4. Transaction APIs

## 4.1 Transfer Between Own Accounts

Transfers money from one customer account to another account owned by the same customer.

```http
POST /transactions/transfer
```

### Request Body

```json
{
  "fromAccountId": "acc_2001",
  "toAccountId": "acc_2002",
  "amount": 100.00,
  "description": "Move money to savings"
}
```

### Success Response — `201 Created`

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

### Kafka Event Published

After a successful transfer, Transaction Service publishes:

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

### Possible Errors

#### Insufficient Balance

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Insufficient balance for transfer",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T10:51:00Z"
}
```

#### Same Account Transfer

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "From account and to account cannot be the same",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T10:52:00Z"
}
```

#### Account Not Found

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Source account not found",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T10:53:00Z"
}
```

---

## 4.2 Get My Transaction History

Returns all transactions for the logged-in customer.

```http
GET /transactions/my
```

### Query Parameters

| Name   | Required | Example        | Description                |
| ------ | -------: | -------------- | -------------------------- |
| page   |       No | `0`            | Page number                |
| size   |       No | `10`           | Page size                  |
| type   |       No | `TRANSFER_OUT` | Filter by transaction type |
| status |       No | `COMPLETED`    | Filter by status           |

### Example Request

```http
GET /api/transactions/my?page=0&size=10&type=TRANSFER_OUT&status=COMPLETED
```

### Success Response — `200 OK`

```json
{
  "content": [
    {
      "transactionId": "txn_3001",
      "fromAccountId": "acc_2001",
      "toAccountId": "acc_2002",
      "amount": 100.00,
      "transactionType": "TRANSFER_OUT",
      "status": "COMPLETED",
      "description": "Move money to savings",
      "createdAt": "2026-05-25T10:50:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 4.3 Get Transactions by Account

Returns transaction history for a specific account.

```http
GET /transactions/account/{accountId}
```

### Path Parameters

| Name      | Required | Example    |
| --------- | -------: | ---------- |
| accountId |      Yes | `acc_2001` |

### Success Response — `200 OK`

```json
[
  {
    "transactionId": "txn_3001",
    "accountId": "acc_2001",
    "amount": 100.00,
    "transactionType": "TRANSFER_OUT",
    "status": "COMPLETED",
    "description": "Move money to savings",
    "createdAt": "2026-05-25T10:50:00Z"
  }
]
```

### Possible Errors

```json
{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "You are not allowed to view transactions for this account",
  "path": "/api/transactions/account/acc_2001",
  "timestamp": "2026-05-25T10:55:00Z"
}
```

---

## 4.4 Get Transaction by ID

Returns transaction details.

```http
GET /transactions/{transactionId}
```

### Path Parameters

| Name          | Required | Example    |
| ------------- | -------: | ---------- |
| transactionId |      Yes | `txn_3001` |

### Success Response — `200 OK`

```json
{
  "transactionId": "txn_3001",
  "customerId": "usr_1001",
  "fromAccountId": "acc_2001",
  "toAccountId": "acc_2002",
  "amount": 100.00,
  "transactionType": "TRANSFER_OUT",
  "status": "COMPLETED",
  "description": "Move money to savings",
  "createdAt": "2026-05-25T10:50:00Z"
}
```

### Possible Errors

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Transaction not found",
  "path": "/api/transactions/txn_9999",
  "timestamp": "2026-05-25T10:56:00Z"
}
```

---

# 5. Notification APIs

## 5.1 Get My Notifications

Returns notifications for the logged-in customer.

```http
GET /notifications/my
```

### Query Parameters

| Name       | Required | Example | Description                      |
| ---------- | -------: | ------- | -------------------------------- |
| unreadOnly |       No | `true`  | Only return unread notifications |

### Example Request

```http
GET /api/notifications/my?unreadOnly=true
```

### Success Response — `200 OK`

```json
[
  {
    "notificationId": "noti_4001",
    "customerId": "usr_1001",
    "type": "TRANSFER_SUCCESS",
    "message": "Your transfer of $100.00 was completed successfully.",
    "status": "UNREAD",
    "createdAt": "2026-05-25T10:50:05Z",
    "readAt": null
  }
]
```

---

## 5.2 Mark Notification as Read

Marks a notification as read.

```http
PATCH /notifications/{notificationId}/read
```

### Path Parameters

| Name           | Required | Example     |
| -------------- | -------: | ----------- |
| notificationId |      Yes | `noti_4001` |

### Success Response — `200 OK`

```json
{
  "notificationId": "noti_4001",
  "status": "READ",
  "readAt": "2026-05-25T11:00:00Z"
}
```

### Possible Errors

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Notification not found",
  "path": "/api/notifications/noti_9999/read",
  "timestamp": "2026-05-25T11:01:00Z"
}
```

---

# 6. Admin APIs

## 6.1 Admin Get All Accounts

Admin-only endpoint.

```http
GET /accounts
```

### Query Parameters

| Name        | Required | Example    | Description              |
| ----------- | -------: | ---------- | ------------------------ |
| customerId  |       No | `usr_1001` | Filter by customer       |
| accountType |       No | `CHECKING` | Filter by type           |
| status      |       No | `ACTIVE`   | Filter by account status |
| page        |       No | `0`        | Page number              |
| size        |       No | `10`       | Page size                |

### Example Request

```http
GET /api/accounts?status=ACTIVE&page=0&size=10
```

### Success Response — `200 OK`

```json
{
  "content": [
    {
      "accountId": "acc_2001",
      "customerId": "usr_1001",
      "accountNumber": "1000002345",
      "accountType": "CHECKING",
      "balance": 500.00,
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 6.2 Admin Update Account Status

Admin can lock, activate, or deactivate an account.

```http
PATCH /accounts/{accountId}/status
```

### Path Parameters

| Name      | Required | Example    |
| --------- | -------: | ---------- |
| accountId |      Yes | `acc_2001` |

### Request Body

```json
{
  "status": "LOCKED",
  "reason": "Suspicious activity detected"
}
```

### Success Response — `200 OK`

```json
{
  "accountId": "acc_2001",
  "status": "LOCKED",
  "updatedAt": "2026-05-25T11:05:00Z"
}
```

### Possible Errors

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Invalid account status",
  "path": "/api/accounts/acc_2001/status",
  "timestamp": "2026-05-25T11:06:00Z"
}
```

---

# 7. Main Workflow Examples

## Workflow 1: Customer Registration and Login

```text
1. Customer registers
POST /api/auth/register

2. Customer logs in
POST /api/auth/login

3. Frontend stores JWT token

4. Frontend uses token for secured requests
Authorization: Bearer <jwt_token>
```

---

## Workflow 2: Admin Creates Account for Customer

```text
1. Admin logs in
POST /api/auth/login

2. Admin creates customer account
POST /api/accounts

3. Account Service saves account in PostgreSQL

4. Optional: Account Service publishes ACCOUNT_CREATED event

5. Notification Service creates customer notification
```

---

## Workflow 3: Customer Views Dashboard

```text
1. Customer opens dashboard

2. React calls:
GET /api/accounts/summary

3. Account Service checks Redis cache

4. If cache exists, return cached summary

5. If cache does not exist, load accounts from PostgreSQL

6. Store result in Redis

7. Return account summary to frontend
```

---

## Workflow 4: Customer Transfers Money

```text
1. Customer submits transfer form

2. React calls:
POST /api/transactions/transfer

3. Transaction Service validates request

4. Transaction Service calls Account Service to verify accounts

5. Account Service updates balances

6. Transaction Service saves transaction record

7. Transaction Service publishes Kafka event

8. Notification Service consumes event

9. Notification Service creates notification

10. Customer sees updated balance and notification
```

---

# 8. Standard Error Response Format

Use the same error format across all services.

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T11:10:00Z",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be greater than 0"
    }
  ]
}
```

---

# 9. Common HTTP Status Codes

|                      Status | Meaning               | Example                                 |
| --------------------------: | --------------------- | --------------------------------------- |
|                    `200 OK` | Request successful    | Login, get accounts                     |
|               `201 Created` | Resource created      | Register user, create account, transfer |
|           `400 Bad Request` | Invalid request       | Negative amount                         |
|          `401 Unauthorized` | Missing/invalid token | No JWT                                  |
|             `403 Forbidden` | User lacks permission | Customer accessing admin API            |
|             `404 Not Found` | Resource not found    | Account not found                       |
|              `409 Conflict` | Duplicate resource    | Email already exists                    |
| `500 Internal Server Error` | Unexpected failure    | Service crash                           |

---

# 10. Recommended MVP Endpoint List

For the 2-week version, prioritize these endpoints first:

```http
POST  /api/auth/register
POST  /api/auth/login
GET   /api/users/me

POST  /api/accounts
GET   /api/accounts/my
GET   /api/accounts/{accountId}
GET   /api/accounts/summary

POST  /api/transactions/transfer
GET   /api/transactions/my
GET   /api/transactions/account/{accountId}

GET   /api/notifications/my
PATCH /api/notifications/{notificationId}/read
```

This is enough to support the main banking workflows: authentication, account viewing, transfers, transaction history, and notifications.
