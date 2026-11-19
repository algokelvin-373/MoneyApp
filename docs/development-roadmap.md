# Development Roadmap

## Phase 1 - Foundation

- Backend clean architecture scaffold.
- Android clean architecture finance package.
- API contract between Android and backend.
- Backend business-rule tests for transaction balance.

## Phase 2 - Persistence

- Add PostgreSQL adapter under repository interfaces.
- Add migrations for users, accounts, transaction codes, and transactions.
- Replace development token/hash implementation with production-grade libraries.
- Add refresh token storage and logout invalidation.

## Phase 3 - Android UI Integration

- Add auth screens and secure token storage.
- Connect dashboard screen to `GetDashboardSummaryUseCase`.
- Add add/edit/delete transaction flows.
- Add Room cache entities for accounts and transactions.

## Phase 4 - Insight

- Spending by category.
- Monthly cash flow.
- Empty, loading, error, and offline states.

