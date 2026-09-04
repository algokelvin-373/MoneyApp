package postgres

import (
	"context"

	"moneyapp/internal/auth"
	"moneyapp/internal/shared"
)

func (s *Store) Create(ctx context.Context, user auth.User) (auth.User, error) {
	if user.ID == "" {
		user.ID = shared.NewID("user")
	}
	err := s.querier(ctx).QueryRow(ctx, `
		INSERT INTO users (id, name, email, password_hash, created_at, updated_at)
		VALUES ($1, $2, LOWER($3), $4, $5, $6)
		RETURNING id, name, email, password_hash, created_at, updated_at`,
		user.ID, user.Name, user.Email, user.PasswordHash, user.CreatedAt, user.UpdatedAt,
	).Scan(&user.ID, &user.Name, &user.Email, &user.PasswordHash, &user.CreatedAt, &user.UpdatedAt)
	return user, mapError(err)
}

func (s *Store) FindByEmail(ctx context.Context, email string) (auth.User, error) {
	return scanUser(s.querier(ctx).QueryRow(ctx, `
		SELECT id, name, email, password_hash, created_at, updated_at
		FROM users
		WHERE email = LOWER($1)`, email))
}

func (s *Store) FindByID(ctx context.Context, id shared.ID) (auth.User, error) {
	return scanUser(s.querier(ctx).QueryRow(ctx, `
		SELECT id, name, email, password_hash, created_at, updated_at
		FROM users
		WHERE id = $1`, id))
}

func scanUser(row rowScanner) (auth.User, error) {
	var user auth.User
	err := row.Scan(&user.ID, &user.Name, &user.Email, &user.PasswordHash, &user.CreatedAt, &user.UpdatedAt)
	return user, mapError(err)
}

var _ auth.Repository = (*Store)(nil)
