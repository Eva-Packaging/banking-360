# Mini Online Banking Portal — Normalized Database Schema

Recommended database: **PostgreSQL**

Recommended microservice database ownership:

```text
user-service          → user_db
account-service       → account_db
transaction-service   → transaction_db
notification-service  → notification_db
```

In true microservices, each service owns its own database. That means you should **avoid physical foreign keys across services**. For example, `account.customer_id` stores the user ID from User Service, but it does not physically reference `users.id` if the tables are in separate service databases.

For a junior 2-week project, you can run all schemas in one PostgreSQL instance locally, but still keep service ownership separated.

---

# 1. User Service Schema

## Tables

```text
users
roles
user_roles
```

---

## Table: users

Stores customer and admin profile information.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Sample Fields

| Field           | Type           | Description                    |
| --------------- | -------------- | ------------------------------ |
| `id`            | `UUID`         | Primary key                    |
| `first_name`    | `VARCHAR(100)` | Customer first name            |
| `last_name`     | `VARCHAR(100)` | Customer last name             |
| `email`         | `VARCHAR(150)` | Unique login email             |
| `password_hash` | `VARCHAR(255)` | Encrypted password             |
| `phone_number`  | `VARCHAR(20)`  | Customer phone number          |
| `status`        | `VARCHAR(30)`  | `ACTIVE`, `INACTIVE`, `LOCKED` |
| `created_at`    | `TIMESTAMP`    | Created date                   |
| `updated_at`    | `TIMESTAMP`    | Last updated date              |

---

## Table: roles

Stores available system roles.

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);
```

## Sample Roles

```text
CUSTOMER
ADMIN
```

---

## Table: user_roles

Join table between users and roles.

```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);
```

## Relationship

```text
users      many-to-many      roles
users      one-to-many       user_roles
roles      one-to-many       user_roles
```

A user can have multiple roles, and a role can belong to many users.

---

# 2. Account Service Schema

## Tables

```text
accounts
account_audit_logs
```

---

## Table: accounts

Stores customer bank accounts.

```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    account_number VARCHAR(30) NOT NULL UNIQUE,
    account_type VARCHAR(30) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Sample Fields

| Field            | Type            | Description                     |
| ---------------- | --------------- | ------------------------------- |
| `id`             | `UUID`          | Primary key                     |
| `customer_id`    | `UUID`          | Logical reference to `users.id` |
| `account_number` | `VARCHAR(30)`   | Unique account number           |
| `account_type`   | `VARCHAR(30)`   | `CHECKING`, `SAVINGS`           |
| `balance`        | `NUMERIC(15,2)` | Current account balance         |
| `status`         | `VARCHAR(30)`   | `ACTIVE`, `INACTIVE`, `LOCKED`  |
| `created_at`     | `TIMESTAMP`     | Created date                    |
| `updated_at`     | `TIMESTAMP`     | Last updated date               |

## Important Note

In a strict microservice setup:

```text
accounts.customer_id references users.id logically
```

But it should not have a physical foreign key to `users.id` if User Service and Account Service use separate databases.

---

## Table: account_audit_logs

Tracks important changes to an account, such as account creation, locking, unlocking, or balance updates.

```sql
CREATE TABLE account_audit_logs (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    changed_by UUID,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_audit_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
);
```

## Sample Fields

| Field        | Type           | Description                            |
| ------------ | -------------- | -------------------------------------- |
| `id`         | `UUID`         | Primary key                            |
| `account_id` | `UUID`         | FK to `accounts.id`                    |
| `action`     | `VARCHAR(50)`  | `CREATED`, `LOCKED`, `BALANCE_UPDATED` |
| `old_value`  | `VARCHAR(255)` | Previous value                         |
| `new_value`  | `VARCHAR(255)` | New value                              |
| `changed_by` | `UUID`         | Logical user ID                        |
| `reason`     | `VARCHAR(255)` | Reason for change                      |
| `created_at` | `TIMESTAMP`    | Change timestamp                       |

## Relationship

```text
accounts one-to-many account_audit_logs
```

One account can have many audit log records.

---

# 3. Transaction Service Schema

## Tables

```text
transactions
transaction_entries
```

This structure is more normalized than storing only one row per transfer. It allows you to represent both sides of the transfer clearly.

---

## Table: transactions

Stores the high-level transaction record.

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    transaction_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Sample Fields

| Field                   | Type            | Description                         |
| ----------------------- | --------------- | ----------------------------------- |
| `id`                    | `UUID`          | Primary key                         |
| `customer_id`           | `UUID`          | Logical reference to `users.id`     |
| `transaction_reference` | `VARCHAR(50)`   | Unique reference number             |
| `transaction_type`      | `VARCHAR(30)`   | `TRANSFER`, `DEPOSIT`, `WITHDRAWAL` |
| `status`                | `VARCHAR(30)`   | `PENDING`, `COMPLETED`, `FAILED`    |
| `amount`                | `NUMERIC(15,2)` | Transaction amount                  |
| `description`           | `VARCHAR(255)`  | Optional transfer description       |
| `failure_reason`        | `VARCHAR(255)`  | Reason if transaction failed        |
| `created_at`            | `TIMESTAMP`     | Created timestamp                   |
| `updated_at`            | `TIMESTAMP`     | Updated timestamp                   |

---

## Table: transaction_entries

Stores debit and credit entries for each transaction.

```sql
CREATE TABLE transaction_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    balance_after NUMERIC(15, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_entries_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
);
```

## Sample Fields

| Field            | Type            | Description                        |
| ---------------- | --------------- | ---------------------------------- |
| `id`             | `UUID`          | Primary key                        |
| `transaction_id` | `UUID`          | FK to `transactions.id`            |
| `account_id`     | `UUID`          | Logical reference to `accounts.id` |
| `entry_type`     | `VARCHAR(20)`   | `DEBIT` or `CREDIT`                |
| `amount`         | `NUMERIC(15,2)` | Entry amount                       |
| `balance_after`  | `NUMERIC(15,2)` | Balance after transaction          |
| `created_at`     | `TIMESTAMP`     | Entry timestamp                    |

## Relationship

```text
transactions one-to-many transaction_entries
```

One transfer creates one transaction record and two transaction entries:

```text
TRANSFER transaction
    ├── DEBIT entry from checking account
    └── CREDIT entry to savings account
```

This is closer to how financial systems usually model money movement.

---

# 4. Notification Service Schema

## Tables

```text
notifications
notification_delivery_logs
```

---

## Table: notifications

Stores customer notifications.

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    event_id UUID,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNREAD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);
```

## Sample Fields

| Field               | Type           | Description                           |
| ------------------- | -------------- | ------------------------------------- |
| `id`                | `UUID`         | Primary key                           |
| `customer_id`       | `UUID`         | Logical reference to `users.id`       |
| `event_id`          | `UUID`         | Kafka event ID                        |
| `notification_type` | `VARCHAR(50)`  | `TRANSFER_SUCCESS`, `ACCOUNT_CREATED` |
| `title`             | `VARCHAR(150)` | Notification title                    |
| `message`           | `VARCHAR(500)` | Notification body                     |
| `status`            | `VARCHAR(30)`  | `UNREAD`, `READ`                      |
| `created_at`        | `TIMESTAMP`    | Created timestamp                     |
| `read_at`           | `TIMESTAMP`    | Read timestamp                        |

---

## Table: notification_delivery_logs

Stores delivery attempts for mock email/SMS notifications.

```sql
CREATE TABLE notification_delivery_logs (
    id UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    channel VARCHAR(30) NOT NULL,
    delivery_status VARCHAR(30) NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message VARCHAR(255),

    CONSTRAINT fk_delivery_notification
        FOREIGN KEY (notification_id)
        REFERENCES notifications(id)
);
```

## Sample Fields

| Field             | Type           | Description                 |
| ----------------- | -------------- | --------------------------- |
| `id`              | `UUID`         | Primary key                 |
| `notification_id` | `UUID`         | FK to `notifications.id`    |
| `channel`         | `VARCHAR(30)`  | `EMAIL`, `SMS`, `IN_APP`    |
| `delivery_status` | `VARCHAR(30)`  | `SENT`, `FAILED`, `PENDING` |
| `attempted_at`    | `TIMESTAMP`    | Delivery attempt time       |
| `error_message`   | `VARCHAR(255)` | Error if failed             |

## Relationship

```text
notifications one-to-many notification_delivery_logs
```

One notification can have many delivery attempts.

---

# 5. Full Relationship Summary

## User Service Relationships

```text
users many-to-many roles
users one-to-many user_roles
roles one-to-many user_roles
```

## Account Service Relationships

```text
customer_id logical reference to users.id

accounts one-to-many account_audit_logs
```

## Transaction Service Relationships

```text
customer_id logical reference to users.id
account_id logical reference to accounts.id

transactions one-to-many transaction_entries
```

## Notification Service Relationships

```text
customer_id logical reference to users.id
event_id logical reference to Kafka event

notifications one-to-many notification_delivery_logs
```

---

# 6. Simplified ERD View

```text
USER SERVICE
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    users     │ 1   * │  user_roles  │ *   1 │    roles     │
└──────────────┘       └──────────────┘       └──────────────┘


ACCOUNT SERVICE
┌──────────────┐       ┌────────────────────┐
│   accounts   │ 1   * │ account_audit_logs │
└──────────────┘       └────────────────────┘
      │
      │ logical customer_id
      ▼
 users.id


TRANSACTION SERVICE
┌──────────────┐       ┌─────────────────────┐
│ transactions │ 1   * │ transaction_entries │
└──────────────┘       └─────────────────────┘
      │                         │
      │ logical customer_id      │ logical account_id
      ▼                         ▼
 users.id                  accounts.id


NOTIFICATION SERVICE
┌───────────────┐       ┌────────────────────────────┐
│ notifications │ 1   * │ notification_delivery_logs │
└───────────────┘       └────────────────────────────┘
      │
      │ logical customer_id
      ▼
 users.id
```

---

# 7. How the Schema Supports Main Features

## Feature: Customer Registration and Login

Supported by:

```text
users
roles
user_roles
```

Flow:

```text
1. User registers.
2. New row is inserted into users.
3. CUSTOMER role is assigned through user_roles.
4. Login checks users.email and users.password_hash.
5. JWT token is created with user ID and role.
```

---

## Feature: Admin Creates Customer Account

Supported by:

```text
accounts
account_audit_logs
```

Flow:

```text
1. Admin creates checking or savings account.
2. Account Service inserts a row into accounts.
3. Account Service creates an audit record in account_audit_logs.
4. Account belongs to customer through customer_id.
```

Example:

```text
users.id = usr_1001

accounts:
id = acc_2001
customer_id = usr_1001
account_type = CHECKING
balance = 500.00
```

---

## Feature: Customer Views Accounts

Supported by:

```text
accounts
```

Flow:

```text
1. Frontend calls GET /accounts/my.
2. Account Service reads customer ID from JWT.
3. Account Service queries accounts by customer_id.
4. All customer accounts are returned.
```

Query example:

```sql
SELECT *
FROM accounts
WHERE customer_id = 'usr_1001'
AND status = 'ACTIVE';
```

---

## Feature: Customer Views Dashboard Summary

Supported by:

```text
accounts
Redis cache
```

Flow:

```text
1. Dashboard requests account summary.
2. Account Service checks Redis first.
3. If cache miss, Account Service queries accounts.
4. Total balance is calculated.
5. Summary is stored in Redis.
```

Database query:

```sql
SELECT customer_id, SUM(balance) AS total_balance
FROM accounts
WHERE customer_id = 'usr_1001'
GROUP BY customer_id;
```

---

## Feature: Customer Transfers Money

Supported by:

```text
accounts
transactions
transaction_entries
account_audit_logs
```

Flow:

```text
1. Customer submits transfer request.
2. Transaction Service creates a transactions row.
3. Account Service validates source and destination accounts.
4. Account Service updates account balances.
5. Transaction Service creates two transaction_entries rows.
6. Transaction status becomes COMPLETED.
7. Kafka event is published.
```

Example transfer:

```text
Transfer $100 from checking to savings
```

`transactions` row:

```text
id: txn_3001
customer_id: usr_1001
transaction_type: TRANSFER
status: COMPLETED
amount: 100.00
```

`transaction_entries` rows:

```text
Entry 1:
transaction_id: txn_3001
account_id: acc_2001
entry_type: DEBIT
amount: 100.00

Entry 2:
transaction_id: txn_3001
account_id: acc_2002
entry_type: CREDIT
amount: 100.00
```

This design makes transaction history easier to query and keeps debit/credit records normalized.

---

## Feature: Customer Views Transaction History

Supported by:

```text
transactions
transaction_entries
```

Flow:

```text
1. Customer opens transaction history.
2. Transaction Service reads customer ID from JWT.
3. Service queries transactions by customer_id.
4. It joins transaction_entries to show related account movements.
```

Query example:

```sql
SELECT 
    t.id,
    t.transaction_reference,
    t.transaction_type,
    t.status,
    t.amount,
    te.account_id,
    te.entry_type,
    te.balance_after,
    t.created_at
FROM transactions t
JOIN transaction_entries te
    ON t.id = te.transaction_id
WHERE t.customer_id = 'usr_1001'
ORDER BY t.created_at DESC;
```

---

## Feature: Kafka Notification After Transfer

Supported by:

```text
notifications
notification_delivery_logs
```

Flow:

```text
1. Transaction Service publishes TRANSFER_COMPLETED event to Kafka.
2. Notification Service consumes event.
3. Notification Service inserts a row into notifications.
4. Notification Service inserts delivery attempt into notification_delivery_logs.
5. Customer can view notification in React UI.
```

Example notification:

```text
notification_type: TRANSFER_SUCCESS
message: Your transfer of $100.00 was completed successfully.
status: UNREAD
```

---

## Feature: Mark Notification as Read

Supported by:

```text
notifications
```

Flow:

```text
1. Customer clicks notification.
2. Frontend calls PATCH /notifications/{id}/read.
3. Notification status changes from UNREAD to READ.
4. read_at timestamp is updated.
```

Update example:

```sql
UPDATE notifications
SET status = 'READ',
    read_at = CURRENT_TIMESTAMP
WHERE id = 'noti_4001';
```

---

# 8. Recommended Indexes

Indexes will help with common queries.

## User Service Indexes

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
```

## Account Service Indexes

```sql
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);
```

## Transaction Service Indexes

```sql
CREATE INDEX idx_transactions_customer_id ON transactions(customer_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transaction_entries_account_id ON transaction_entries(account_id);
CREATE INDEX idx_transaction_entries_transaction_id ON transaction_entries(transaction_id);
```

## Notification Service Indexes

```sql
CREATE INDEX idx_notifications_customer_id ON notifications(customer_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

---

# 9. Recommended Enum Values

You can model these as Java enums and store them as strings in PostgreSQL.

## User Status

```text
ACTIVE
INACTIVE
LOCKED
```

## Role Name

```text
CUSTOMER
ADMIN
```

## Account Type

```text
CHECKING
SAVINGS
```

## Account Status

```text
ACTIVE
INACTIVE
LOCKED
CLOSED
```

## Transaction Type

```text
TRANSFER
DEPOSIT
WITHDRAWAL
```

## Transaction Status

```text
PENDING
COMPLETED
FAILED
```

## Entry Type

```text
DEBIT
CREDIT
```

## Notification Status

```text
UNREAD
READ
```

## Notification Type

```text
TRANSFER_SUCCESS
TRANSFER_FAILED
ACCOUNT_CREATED
ACCOUNT_LOCKED
```

---

# 10. MVP Schema Recommendation

For the 2-week version, start with these tables only:

```text
users
roles
user_roles
accounts
transactions
transaction_entries
notifications
```

Add these if time allows:

```text
account_audit_logs
notification_delivery_logs
```

This keeps the project simple enough for junior developers while still showing a clean, normalized, banking-style database design.
