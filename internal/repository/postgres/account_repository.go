package postgres

import (
	"context"
	"errors"

	"github.com/jackc/pgx/v5"

	"moneyapp/internal/account"
	"moneyapp/internal/shared"
)

type AccountRepository struct {
	store *Store
}

func NewAccountRepository(store *Store) AccountRepository {
	return AccountRepository{store: store}
}

func (r AccountRepository) ListByUser(ctx context.Context, userID shared.ID) ([]account.Account, error) {
	rows, err := r.store.querier(ctx).Query(ctx, `
		SELECT id, user_id, code, name, balance, is_active, created_at, updated_at
		FROM accounts
		WHERE user_id = $1
		ORDER BY code`, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]account.Account, 0)
	for rows.Next() {
		item, err := scanAccount(rows)
		if err != nil {
			return nil, err
		}
		items = append(items, item)
	}
	return items, rows.Err()
}

func (r AccountRepository) FindByIDForUser(ctx context.Context, id, userID shared.ID) (account.Account, error) {
	return scanAccount(r.store.querier(ctx).QueryRow(ctx, `
		SELECT id, user_id, code, name, balance, is_active, created_at, updated_at
		FROM accounts
		WHERE id = $1 AND user_id = $2`, id, userID))
}

func (r AccountRepository) FindByCodeForUser(ctx context.Context, code string, userID shared.ID) (account.Account, error) {
	return scanAccount(r.store.querier(ctx).QueryRow(ctx, `
		SELECT id, user_id, code, name, balance, is_active, created_at, updated_at
		FROM accounts
		WHERE code = $1 AND user_id = $2`, code, userID))
}

func (r AccountRepository) Create(ctx context.Context, item account.Account) (account.Account, error) {
	if item.ID == "" {
		item.ID = shared.NewID("account")
	}
	err := r.store.querier(ctx).QueryRow(ctx, `
		INSERT INTO accounts (id, user_id, code, name, balance, is_active, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id, user_id, code, name, balance, is_active, created_at, updated_at`,
		item.ID, item.UserID, item.Code, item.Name, item.Balance, item.IsActive, item.CreatedAt, item.UpdatedAt,
	).Scan(&item.ID, &item.UserID, &item.Code, &item.Name, &item.Balance, &item.IsActive, &item.CreatedAt, &item.UpdatedAt)
	return item, mapError(err)
}

func (r AccountRepository) Update(ctx context.Context, item account.Account) (account.Account, error) {
	err := r.store.querier(ctx).QueryRow(ctx, `
		UPDATE accounts
		SET code = $3, name = $4, balance = $5, is_active = $6, updated_at = $7
		WHERE id = $1 AND user_id = $2
		RETURNING id, user_id, code, name, balance, is_active, created_at, updated_at`,
		item.ID, item.UserID, item.Code, item.Name, item.Balance, item.IsActive, item.UpdatedAt,
	).Scan(&item.ID, &item.UserID, &item.Code, &item.Name, &item.Balance, &item.IsActive, &item.CreatedAt, &item.UpdatedAt)
	return item, mapError(err)
}

func (r AccountRepository) AdjustBalance(ctx context.Context, id, userID shared.ID, delta int64) (account.Account, error) {
	item, err := scanAccount(r.store.querier(ctx).QueryRow(ctx, `
		UPDATE accounts
		SET balance = balance + $3, updated_at = NOW()
		WHERE id = $1 AND user_id = $2 AND is_active = TRUE AND balance + $3 >= 0
		RETURNING id, user_id, code, name, balance, is_active, created_at, updated_at`, id, userID, delta))
	if !errors.Is(err, shared.ErrNotFound) {
		return item, err
	}

	var exists bool
	err = r.store.querier(ctx).QueryRow(ctx,
		`SELECT EXISTS (SELECT 1 FROM accounts WHERE id = $1 AND user_id = $2)`, id, userID,
	).Scan(&exists)
	if err != nil && !errors.Is(err, pgx.ErrNoRows) {
		return account.Account{}, err
	}
	if exists && delta < 0 {
		return account.Account{}, shared.ErrInsufficientBalance
	}
	return account.Account{}, shared.ErrNotFound
}

func scanAccount(row rowScanner) (account.Account, error) {
	var item account.Account
	err := row.Scan(&item.ID, &item.UserID, &item.Code, &item.Name, &item.Balance, &item.IsActive, &item.CreatedAt, &item.UpdatedAt)
	return item, mapError(err)
}

var _ account.Repository = AccountRepository{}
