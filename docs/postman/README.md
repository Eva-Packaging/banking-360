# Banking 360 — Postman Collection

API testing collection for the Banking 360 microservices platform.

## Files

| File | Description |
|------|-------------|
| `banking360.postman_collection.json` | Main collection — all 17 requests across 6 service folders |
| `banking360-dev.postman_environment.json` | Dev environment — `http://localhost:8080/api` |
| `banking360-prod.postman_environment.json` | Prod environment — `https://api.banking360.com/api` |

---

## Import

1. Open Postman and click **Import** (top-left)
2. Drag and drop all three `.json` files at once, or use **Select Files** to pick them
3. Postman will create the collection and both environments

---

## Environment Setup

Select an environment from the environment dropdown (top-right corner of Postman) before running any requests.

| Variable | Dev | Prod |
|----------|-----|------|
| `protocol` | `http` | `https` |
| `host` | `localhost` | `api.banking360.com` |
| `port` | `8080` | `443` |
| `baseUrl` | `http://localhost:8080/api` | `https://api.banking360.com/api` |

> **Prod note:** Update `host` in the Prod environment to match your actual deployed domain before use.

---

## Authentication

The collection uses Bearer token auth at the collection level. All requests inherit it automatically — you do not need to set the `Authorization` header manually.

**How tokens are managed:**

1. Run **1. Auth Service → Login**
2. The test script extracts `accessToken` from the response and saves it to `{{jwt_token}}`
3. Every subsequent request in the collection picks it up automatically

The **Register** and **Login** requests are set to `noauth` so they are never blocked by a missing token.

If your token expires, re-run Login and the collection variable is refreshed.

---

## Variable Auto-Population

Test scripts on each request capture IDs from responses and save them as collection variables. Running requests in order means you rarely need to fill in IDs manually.

| Variable | Set by |
|----------|--------|
| `jwt_token` | Login |
| `userId` | Register or Login |
| `accountId` | Create Account or Get My Accounts (index 0) |
| `secondAccountId` | Create Second Account or Get My Accounts (index 1) |
| `transactionId` | Transfer or Get My Transaction History (index 0) |
| `notificationId` | Get My Notifications (index 0) |

To set a variable manually: open the environment editor (click the eye icon → Edit) and enter the value directly.

---

## Recommended Test Flow

Run requests in this order for the full end-to-end workflow. Each step builds on the previous one.

```
1. Register Customer          → saves userId
2. Login                      → saves jwt_token, userId
3. Create Account             → saves accountId
4. Create Second Account      → saves secondAccountId
5. Get My Accounts            → confirms both accounts visible
6. Get Account Summary        → verify Redis cache behaviour (check cached field)
7. Transfer Between Accounts  → saves transactionId; triggers Kafka event
8. Get My Transaction History → confirm transfer appears
9. Get My Notifications       → saves notificationId (populated by Kafka consumer)
10. Mark Notification as Read → verify status flips to READ
```

> Steps 3 and 4 require an **Admin** JWT. Log in with an admin account before running them, then log back in as a customer to continue from step 5.

---

## Request Reference

### 1. Auth Service

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | None | Create customer account |
| POST | `/auth/login` | None | Authenticate and receive JWT |

### 2. User Service

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/users/me` | Customer / Admin | Get logged-in user profile |
| GET | `/users` | Admin only | List all customers (paginated) |

### 3. Account Service

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/accounts` | Admin only | Create account for a customer |
| GET | `/accounts/my` | Customer | List own accounts |
| GET | `/accounts/{accountId}` | Customer | Get single account |
| GET | `/accounts/summary` | Customer | Dashboard summary (Redis-cached) |

### 4. Transaction Service

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/transactions/transfer` | Customer | Transfer between own accounts |
| GET | `/transactions/my` | Customer | Paginated transaction history |
| GET | `/transactions/account/{accountId}` | Customer | Transactions for one account |
| GET | `/transactions/{transactionId}` | Customer | Single transaction detail |

### 5. Notification Service

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/notifications/my` | Customer | List notifications (`unreadOnly` param) |
| PATCH | `/notifications/{notificationId}/read` | Customer | Mark notification as read |

### 6. Admin

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/accounts` | Admin only | All accounts across all customers |
| PATCH | `/accounts/{accountId}/status` | Admin only | Lock / activate / deactivate account |
| GET | `/transactions/admin` | Admin only | All transactions across all customers |

---

## Query Parameters

Several requests include optional query parameters that are **disabled by default**. To use them:

1. Open the request
2. Go to the **Params** tab
3. Tick the checkbox next to the parameter you want to enable

Common optional params:

| Endpoint | Param | Values |
|----------|-------|--------|
| `GET /transactions/my` | `type` | `TRANSFER_OUT`, `TRANSFER_IN` |
| `GET /transactions/my` | `status` | `COMPLETED`, `FAILED`, `PENDING` |
| `GET /accounts` (admin) | `accountType` | `CHECKING`, `SAVINGS` |
| `GET /accounts` (admin) | `customerId` | any userId |
| `GET /notifications/my` | `unreadOnly` | `true`, `false` |

---

## Standard Error Shape

All services return errors in this format:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Insufficient balance for transfer",
  "path": "/api/transactions/transfer",
  "timestamp": "2026-05-25T10:51:00Z"
}
```

Common status codes: `400` bad input, `401` missing/expired token, `403` wrong role, `404` resource not found, `409` duplicate (e.g. email already registered).

---

## Local Dev Prerequisites

The full stack must be running before hitting any endpoint. Start all services with:

```bash
docker compose up -d
```

Services and their default ports:

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| user-service | 8081 |
| account-service | 8082 |
| transaction-service | 8083 |
| notification-service | 8084 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Kafka | 9092 |

All requests route through the gateway on port 8080 — you do not need to call individual service ports directly.
