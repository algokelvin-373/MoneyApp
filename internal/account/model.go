package account

import (
	"context"
	"time"

	"moneyapp/internal/shared"
)

type Account struct {
	ID        shared.ID `json:"id"`
	UserID    shared.ID `json:"user_id"`
	Code      string    `json:"code"`
	Name      string    `json:"name"`
	Balance   int64     `json:"balance"`
	IsActive  bool      `json:"is_active"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type Repository interface {
	ListByUser(ctx context.Context, userID shared.ID) ([]Account, error)
	FindByIDForUser(ctx context.Context, id, userID shared.ID) (Account, error)
	FindByCodeForUser(ctx context.Context, code string, userID shared.ID) (Account, error)
	Create(ctx context.Context, account Account) (Account, error)
	Update(ctx context.Context, account Account) (Account, error)
	AdjustBalance(ctx context.Context, id, userID shared.ID, delta int64) (Account, error)
}

type Service struct {
	repo  Repository
	clock shared.Clock
}

func NewService(repo Repository, clock shared.Clock) *Service {
	return &Service{repo: repo, clock: clock}
}

func (s *Service) List(ctx context.Context, userID shared.ID) ([]Account, error) {
	return s.repo.ListByUser(ctx, userID)
}

func (s *Service) Get(ctx context.Context, id, userID shared.ID) (Account, error) {
	return s.repo.FindByIDForUser(ctx, id, userID)
}

func (s *Service) CreateDefaultAccounts(ctx context.Context, userID shared.ID) error {
	defaults := []struct {
		code string
		name string
	}{
		{"WALLET", "Dompet"},
		{"GOPAY", "GoPay"},
		{"OVO", "OVO"},
	}

	now := s.clock.Now()
	for _, item := range defaults {
		_, err := s.repo.Create(ctx, Account{
			ID:        "",
			UserID:    userID,
			Code:      item.code,
			Name:      item.name,
			Balance:   0,
			IsActive:  true,
			CreatedAt: now,
			UpdatedAt: now,
		})
		if err != nil {
			return err
		}
	}
	return nil
}
