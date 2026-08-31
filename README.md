# MoneyApp

Personal Financial Literacy & Expense Management Platform.

This repository contains:

- `money_app_android/`: Android native Kotlin app.
- Backend Go REST API outside `money_app_android/`.

## Backend

Run:

```bash
go run ./cmd/api
```

Base URL:

```text
http://localhost:8080/api/v1
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

Current backend adapter is in-memory for fast portfolio iteration. PostgreSQL should be added behind repository interfaces without changing use cases.

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

