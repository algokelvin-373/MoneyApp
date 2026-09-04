//go:build integration

package postgres

import (
	"context"
	"errors"
	"os"
	"testing"
	"time"

	"moneyapp/internal/auth"
	"moneyapp/internal/shared"
)

func TestStoreMigrationAndUnitOfWorkRollback(t *testing.T) {
	databaseURL := os.Getenv("DATABASE_URL")
	if databaseURL == "" {
		t.Skip("DATABASE_URL is required for PostgreSQL integration tests")
	}

	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()
	store, err := NewStore(ctx, databaseURL)
	if err != nil {
		t.Fatal(err)
	}
	defer store.Close()
	if err := store.Migrate(ctx); err != nil {
		t.Fatal(err)
	}

	var codeCount int
	if err := store.pool.QueryRow(ctx, `SELECT COUNT(*) FROM transaction_codes`).Scan(&codeCount); err != nil {
		t.Fatal(err)
	}
	if codeCount != 14 {
		t.Fatalf("expected 14 transaction codes, got %d", codeCount)
	}

	userID := shared.NewID("rollback-user")
	rollbackError := errors.New("force rollback")
	err = store.UnitOfWork().Do(ctx, func(txCtx context.Context) error {
		_, createErr := store.Create(txCtx, auth.User{
			ID:           userID,
			Name:         "Rollback Test",
			Email:        string(userID) + "@example.com",
			PasswordHash: "test-hash",
			CreatedAt:    time.Now().UTC(),
			UpdatedAt:    time.Now().UTC(),
		})
		if createErr != nil {
			return createErr
		}
		return rollbackError
	})
	if !errors.Is(err, rollbackError) {
		t.Fatalf("expected rollback error, got %v", err)
	}
	if _, err := store.FindByID(ctx, userID); !errors.Is(err, shared.ErrNotFound) {
		t.Fatalf("expected rolled back user to be absent, got %v", err)
	}
}
