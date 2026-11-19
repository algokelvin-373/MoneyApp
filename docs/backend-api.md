# Backend API Contract

Base path:

```text
/api/v1
```

All protected endpoints require:

```text
Authorization: Bearer <access_token>
```

## Auth

```text
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
```

Register request:

```json
{
  "name": "Kelvin",
  "email": "kelvin@example.com",
  "password": "password123"
}
```

## Accounts

```text
GET /accounts
GET /accounts/{id}
```

Default accounts are created after registration:

```text
WALLET
GOPAY
OVO
```

## Transaction Codes

```text
GET /transaction-codes
GET /transaction-codes?type=EXPENSE
GET /transaction-codes?type=INCOME
```

## Transactions

```text
GET    /transactions
GET    /transactions/{id}
POST   /transactions
PATCH  /transactions/{id}
DELETE /transactions/{id}
```

Create request:

```json
{
  "account_id": "account-id",
  "code": "FOOD",
  "transaction_type": "CREDIT",
  "amount": 25000,
  "transaction_date": "2026-08-31T10:00:00Z",
  "note": "Makan siang",
  "idempotency_key": "client-generated-key"
}
```

Rules:

- `DEBIT` requires an income code.
- `CREDIT` requires an expense code.
- `amount` must be greater than 0.
- `CREDIT` cannot make account balance negative.
- Delete is soft delete.

## Dashboard

```text
GET /dashboard/summary
GET /dashboard/summary?start_date=2026-08-01&end_date=2026-08-31
```

## Insight

```text
GET /insights/spending
GET /insights/spending?start_date=2026-08-01&end_date=2026-08-31
```

