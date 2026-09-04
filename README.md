# MoneyApp

Personal Financial Literacy & Expense Management Platform.

This repository contains:

- `money_app_android/`: Android native Kotlin app.
- Backend Go REST API outside `money_app_android/`.

## Backend

Run with the in-memory adapter:

```bash
go run ./cmd/api
```

Base URL:

```text
http://localhost:8080/api/v1
```

### PostgreSQL

Start PostgreSQL:

```powershell
docker compose up -d postgres
```

Configure the backend in PowerShell and run it:

```powershell
$env:DATABASE_URL="postgres://moneyapp:moneyapp@localhost:5432/moneyapp?sslmode=disable"
$env:JWT_SECRET="replace-with-a-long-random-secret"
go run ./cmd/api
```

The backend checks the connection and applies the idempotent schema migration on startup. The SQL schema is in `internal/repository/postgres/migrations/000001_init.sql`.

Connect manually with `psql`:

```powershell
psql "postgres://moneyapp:moneyapp@localhost:5432/moneyapp?sslmode=disable"
```

Implemented foundation:

- Register and login.
- Default accounts: `WALLET`, `GOPAY`, `OVO`.
- Transaction code master data from the PRD.
- Transaction create, list, detail, update, and soft delete.
- Dashboard summary.
- Spending insight by category.
- Standard API response envelope.
- Clean domain/service/repository boundaries.

The backend supports both in-memory and PostgreSQL repository adapters. PostgreSQL is selected when `DATABASE_URL` is present.

## Android

The Android project keeps existing screens and now includes a clean finance foundation:

- `core/network`: Retrofit client and standard response model.
- `feature/finance/domain`: models, repository contract, use case.
- `feature/finance/data`: Retrofit API service and remote repository.

Emulator backend base URL defaults to:

```text
http://10.0.2.2:8080/api/v1/
```

## Verification

Backend:

```bash
go test ./...
```

Android:

```bash
cd money_app_android
./gradlew.bat :app:assembleDebug
```

