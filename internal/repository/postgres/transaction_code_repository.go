package postgres

import (
	"context"

	"moneyapp/internal/shared"
	"moneyapp/internal/transactioncode"
)

type TransactionCodeRepository struct {
	store *Store
}

func NewTransactionCodeRepository(store *Store) TransactionCodeRepository {
	return TransactionCodeRepository{store: store}
}

func (r TransactionCodeRepository) List(ctx context.Context, codeType *transactioncode.CodeType) ([]transactioncode.TransactionCode, error) {
	query := `SELECT id, code, name, type, is_active, created_at, updated_at FROM transaction_codes`
	args := make([]any, 0, 1)
	if codeType != nil {
		query += ` WHERE type = $1`
		args = append(args, *codeType)
	}
	query += ` ORDER BY code`

	rows, err := r.store.querier(ctx).Query(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	items := make([]transactioncode.TransactionCode, 0)
	for rows.Next() {
		item, err := scanTransactionCode(rows)
		if err != nil {
			return nil, err
		}
		items = append(items, item)
	}
	return items, rows.Err()
}

func (r TransactionCodeRepository) FindByID(ctx context.Context, id shared.ID) (transactioncode.TransactionCode, error) {
	return scanTransactionCode(r.store.querier(ctx).QueryRow(ctx, `
		SELECT id, code, name, type, is_active, created_at, updated_at
		FROM transaction_codes WHERE id = $1`, id))
}

func (r TransactionCodeRepository) FindByCode(ctx context.Context, code string) (transactioncode.TransactionCode, error) {
	return scanTransactionCode(r.store.querier(ctx).QueryRow(ctx, `
		SELECT id, code, name, type, is_active, created_at, updated_at
		FROM transaction_codes WHERE code = $1`, code))
}

func scanTransactionCode(row rowScanner) (transactioncode.TransactionCode, error) {
	var item transactioncode.TransactionCode
	err := row.Scan(&item.ID, &item.Code, &item.Name, &item.Type, &item.IsActive, &item.CreatedAt, &item.UpdatedAt)
	return item, mapError(err)
}

var _ transactioncode.Repository = TransactionCodeRepository{}
