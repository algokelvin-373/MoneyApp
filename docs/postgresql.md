# PostgreSQL Development Setup

## Local database

Create the development role and database with PostgreSQL 16:

```powershell
& 'C:\Program Files\PostgreSQL\16\bin\psql.exe' `
  -d 'postgresql://postgres:postgres@127.0.0.1:5432/postgres?sslmode=disable' `
  -v ON_ERROR_STOP=1 `
  -f .\scripts\postgres\bootstrap.sql
```

Configure and run the API:

```powershell
$env:DATABASE_URL='postgres://moneyapp:moneyapp@127.0.0.1:5432/moneyapp?sslmode=disable'
$env:JWT_SECRET='replace-with-a-long-random-secret'
go run .\cmd\api
```

When `DATABASE_URL` is set, startup connects through `pgxpool` and applies `internal/repository/postgres/migrations/000001_init.sql`. Without it, the API uses the in-memory adapter.

## Useful queries

Connect as the application user:

```powershell
& 'C:\Program Files\PostgreSQL\16\bin\psql.exe' `
  -d 'postgresql://moneyapp:moneyapp@127.0.0.1:5432/moneyapp?sslmode=disable'
```

Inspect the schema and seed data:

```sql
\dt

SELECT code, name, type, is_active
FROM transaction_codes
ORDER BY type, code;
```

Inspect user balances:

```sql
SELECT u.email, a.code, a.name, a.balance, a.is_active
FROM users AS u
JOIN accounts AS a ON a.user_id = u.id
ORDER BY u.email, a.code;
```

Inspect active transaction history:

```sql
SELECT
    t.transaction_date,
    u.email,
    a.code AS account_code,
    tc.code AS category_code,
    t.transaction_type,
    t.amount,
    t.note
FROM transactions AS t
JOIN users AS u ON u.id = t.user_id
JOIN accounts AS a ON a.id = t.account_id
JOIN transaction_codes AS tc ON tc.id = t.transaction_code_id
WHERE t.deleted_at IS NULL
ORDER BY t.transaction_date DESC;
```

Run PostgreSQL integration tests:

```powershell
$env:DATABASE_URL='postgres://moneyapp:moneyapp@127.0.0.1:5432/moneyapp?sslmode=disable'
go test -tags=integration ./internal/repository/postgres -v
```
