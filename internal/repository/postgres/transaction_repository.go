package postgres

import (
	"context"
	"fmt"
	"strings"
	"time"

	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
)

const transactionColumns = `
	t.id, t.user_id, t.account_id, a.code, t.transaction_code_id, c.code,
	t.transaction_type, t.amount, t.transaction_date, t.note, t.idempotency_key,
	t.created_at, t.updated_at, t.deleted_at`

type TransactionRepository struct {
	store *Store
}

func NewTransactionRepository(store *Store) TransactionRepository {
	return TransactionRepository{store: store}
}

func (r TransactionRepository) Create(ctx context.Context, item transaction.Transaction) (transaction.Transaction, error) {
	_, err := r.store.querier(ctx).Exec(ctx, `
		INSERT INTO transactions (
			id, user_id, account_id, transaction_code_id, transaction_type, amount,
			transaction_date, note, idempotency_key, created_at, updated_at, deleted_at
		) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)`,
		item.ID, item.UserID, item.AccountID, item.TransactionCodeID, item.Type, item.Amount,
		item.TransactionDate, item.Note, item.IdempotencyKey, item.CreatedAt, item.UpdatedAt, item.DeletedAt,
	)
	return item, mapError(err)
}

func (r TransactionRepository) Update(ctx context.Context, item transaction.Transaction) (transaction.Transaction, error) {
	tag, err := r.store.querier(ctx).Exec(ctx, `
		UPDATE transactions
		SET account_id = $3, transaction_code_id = $4, transaction_type = $5,
			amount = $6, transaction_date = $7, note = $8, updated_at = $9
		WHERE id = $1 AND user_id = $2 AND deleted_at IS NULL`,
		item.ID, item.UserID, item.AccountID, item.TransactionCodeID, item.Type,
		item.Amount, item.TransactionDate, item.Note, item.UpdatedAt,
	)
	if err != nil {
		return transaction.Transaction{}, mapError(err)
	}
	if tag.RowsAffected() == 0 {
		return transaction.Transaction{}, shared.ErrNotFound
	}
	return item, nil
}

func (r TransactionRepository) FindByIDForUser(ctx context.Context, id, userID shared.ID) (transaction.Transaction, error) {
	query := `SELECT ` + transactionColumns + `
		FROM transactions t
		JOIN accounts a ON a.id = t.account_id
		JOIN transaction_codes c ON c.id = t.transaction_code_id
		WHERE t.id = $1 AND t.user_id = $2 AND t.deleted_at IS NULL`
	return scanTransaction(r.store.querier(ctx).QueryRow(ctx, query, id, userID))
}

func (r TransactionRepository) FindByIdempotencyKey(ctx context.Context, userID shared.ID, key string) (transaction.Transaction, error) {
	if key == "" {
		return transaction.Transaction{}, shared.ErrNotFound
	}
	query := `SELECT ` + transactionColumns + `
		FROM transactions t
		JOIN accounts a ON a.id = t.account_id
		JOIN transaction_codes c ON c.id = t.transaction_code_id
		WHERE t.user_id = $1 AND t.idempotency_key = $2 AND t.deleted_at IS NULL`
	return scanTransaction(r.store.querier(ctx).QueryRow(ctx, query, userID, key))
}

func (r TransactionRepository) List(ctx context.Context, filter transaction.Filter) ([]transaction.Transaction, error) {
	conditions := []string{"t.user_id = $1", "t.deleted_at IS NULL"}
	args := []any{filter.UserID}
	addCondition := func(condition string, value any) {
		args = append(args, value)
		conditions = append(conditions, fmt.Sprintf(condition, len(args)))
	}
	if filter.Account != "" {
		addCondition("a.code = $%d", filter.Account)
	}
	if filter.Code != "" {
		addCondition("c.code = $%d", filter.Code)
	}
	if filter.Type != "" {
		addCondition("t.transaction_type = $%d", filter.Type)
	}
	if filter.StartDate != nil {
		addCondition("t.transaction_date >= $%d", *filter.StartDate)
	}
	if filter.EndDate != nil {
		addCondition("t.transaction_date <= $%d", *filter.EndDate)
	}

	args = append(args, filter.Limit, filter.Offset)
	query := `SELECT ` + transactionColumns + `
		FROM transactions t
		JOIN accounts a ON a.id = t.account_id
		JOIN transaction_codes c ON c.id = t.transaction_code_id
		WHERE ` + strings.Join(conditions, " AND ") + fmt.Sprintf(`
		ORDER BY t.transaction_date DESC
		LIMIT $%d OFFSET $%d`, len(args)-1, len(args))

	rows, err := r.store.querier(ctx).Query(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	items := make([]transaction.Transaction, 0)
	for rows.Next() {
		item, err := scanTransaction(rows)
		if err != nil {
			return nil, err
		}
		items = append(items, item)
	}
	return items, rows.Err()
}

func (r TransactionRepository) SoftDelete(ctx context.Context, id, userID shared.ID, deletedAt time.Time) error {
	tag, err := r.store.querier(ctx).Exec(ctx, `
		UPDATE transactions
		SET deleted_at = $3, updated_at = $3
		WHERE id = $1 AND user_id = $2 AND deleted_at IS NULL`, id, userID, deletedAt)
	if err != nil {
		return err
	}
	if tag.RowsAffected() == 0 {
		return shared.ErrNotFound
	}
	return nil
}

func scanTransaction(row rowScanner) (transaction.Transaction, error) {
	var item transaction.Transaction
	err := row.Scan(
		&item.ID, &item.UserID, &item.AccountID, &item.AccountCode,
		&item.TransactionCodeID, &item.Code, &item.Type, &item.Amount,
		&item.TransactionDate, &item.Note, &item.IdempotencyKey,
		&item.CreatedAt, &item.UpdatedAt, &item.DeletedAt,
	)
	return item, mapError(err)
}

var _ transaction.Repository = TransactionRepository{}
