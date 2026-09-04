package transaction

import (
	"context"
	"strings"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/shared"
	"moneyapp/internal/transactioncode"
)

type Type string

const (
	TypeDebit  Type = "DEBIT"
	TypeCredit Type = "CREDIT"
)

type Transaction struct {
	ID                shared.ID  `json:"id"`
	UserID            shared.ID  `json:"user_id"`
	AccountID         shared.ID  `json:"account_id"`
	AccountCode       string     `json:"account_code"`
	TransactionCodeID shared.ID  `json:"transaction_code_id"`
	Code              string     `json:"code"`
	Type              Type       `json:"transaction_type"`
	Amount            int64      `json:"amount"`
	TransactionDate   time.Time  `json:"transaction_date"`
	Note              string     `json:"note"`
	IdempotencyKey    string     `json:"idempotency_key"`
	CreatedAt         time.Time  `json:"created_at"`
	UpdatedAt         time.Time  `json:"updated_at"`
	DeletedAt         *time.Time `json:"deleted_at,omitempty"`
}

type Filter struct {
	UserID    shared.ID
	Account   string
	Code      string
	Type      Type
	StartDate *time.Time
	EndDate   *time.Time
	Limit     int
	Offset    int
}

type Repository interface {
	Create(ctx context.Context, tx Transaction) (Transaction, error)
	Update(ctx context.Context, tx Transaction) (Transaction, error)
	FindByIDForUser(ctx context.Context, id, userID shared.ID) (Transaction, error)
	FindByIdempotencyKey(ctx context.Context, userID shared.ID, key string) (Transaction, error)
	List(ctx context.Context, filter Filter) ([]Transaction, error)
	SoftDelete(ctx context.Context, id, userID shared.ID, deletedAt time.Time) error
}

type UnitOfWork interface {
	Do(ctx context.Context, fn func(context.Context) error) error
}

type Service struct {
	transactions Repository
	accounts     account.Repository
	codes        transactioncode.Repository
	uow          UnitOfWork
	clock        shared.Clock
}

func NewService(transactions Repository, accounts account.Repository, codes transactioncode.Repository, uow UnitOfWork, clock shared.Clock) *Service {
	return &Service{transactions: transactions, accounts: accounts, codes: codes, uow: uow, clock: clock}
}

type CreateInput struct {
	UserID          shared.ID `json:"-"`
	AccountID       shared.ID `json:"account_id"`
	Code            string    `json:"code"`
	Type            Type      `json:"transaction_type"`
	Amount          int64     `json:"amount"`
	TransactionDate time.Time `json:"transaction_date"`
	Note            string    `json:"note"`
	IdempotencyKey  string    `json:"idempotency_key"`
}

type UpdateInput struct {
	UserID          shared.ID `json:"-"`
	ID              shared.ID `json:"-"`
	AccountID       shared.ID `json:"account_id"`
	Code            string    `json:"code"`
	Type            Type      `json:"transaction_type"`
	Amount          int64     `json:"amount"`
	TransactionDate time.Time `json:"transaction_date"`
	Note            string    `json:"note"`
}

func (s *Service) Create(ctx context.Context, input CreateInput) (Transaction, error) {
	if input.IdempotencyKey != "" {
		existing, err := s.transactions.FindByIdempotencyKey(ctx, input.UserID, input.IdempotencyKey)
		if err == nil {
			return existing, nil
		}
	}

	var created Transaction
	err := s.uow.Do(ctx, func(txCtx context.Context) error {
		acc, code, err := s.validate(txCtx, input.UserID, input.AccountID, input.Code, input.Type, input.Amount, input.TransactionDate)
		if err != nil {
			return err
		}
		delta := balanceDelta(input.Type, input.Amount)
		if input.Type == TypeCredit && acc.Balance+delta < 0 {
			return shared.ErrInsufficientBalance
		}
		if _, err := s.accounts.AdjustBalance(txCtx, acc.ID, input.UserID, delta); err != nil {
			return err
		}
		now := s.clock.Now()
		created, err = s.transactions.Create(txCtx, Transaction{
			ID:                shared.NewID("trx"),
			UserID:            input.UserID,
			AccountID:         acc.ID,
			AccountCode:       acc.Code,
			TransactionCodeID: code.ID,
			Code:              code.Code,
			Type:              input.Type,
			Amount:            input.Amount,
			TransactionDate:   input.TransactionDate,
			Note:              strings.TrimSpace(input.Note),
			IdempotencyKey:    strings.TrimSpace(input.IdempotencyKey),
			CreatedAt:         now,
			UpdatedAt:         now,
		})
		return err
	})
	return created, err
}

func (s *Service) Update(ctx context.Context, input UpdateInput) (Transaction, error) {
	var updated Transaction
	err := s.uow.Do(ctx, func(txCtx context.Context) error {
		oldTx, err := s.transactions.FindByIDForUser(txCtx, input.ID, input.UserID)
		if err != nil {
			return err
		}
		newAcc, code, err := s.validate(txCtx, input.UserID, input.AccountID, input.Code, input.Type, input.Amount, input.TransactionDate)
		if err != nil {
			return err
		}
		oldAcc, err := s.accounts.FindByIDForUser(txCtx, oldTx.AccountID, input.UserID)
		if err != nil {
			return err
		}
		delta := balanceDelta(input.Type, input.Amount)
		availableNewBalance := newAcc.Balance
		if oldAcc.ID == newAcc.ID {
			availableNewBalance -= balanceDelta(oldTx.Type, oldTx.Amount)
		}
		if input.Type == TypeCredit && availableNewBalance+delta < 0 {
			return shared.ErrInsufficientBalance
		}
		if _, err := s.accounts.AdjustBalance(txCtx, oldAcc.ID, input.UserID, -balanceDelta(oldTx.Type, oldTx.Amount)); err != nil {
			return err
		}
		if _, err := s.accounts.AdjustBalance(txCtx, newAcc.ID, input.UserID, delta); err != nil {
			return err
		}
		oldTx.AccountID = newAcc.ID
		oldTx.AccountCode = newAcc.Code
		oldTx.TransactionCodeID = code.ID
		oldTx.Code = code.Code
		oldTx.Type = input.Type
		oldTx.Amount = input.Amount
		oldTx.TransactionDate = input.TransactionDate
		oldTx.Note = strings.TrimSpace(input.Note)
		oldTx.UpdatedAt = s.clock.Now()
		updated, err = s.transactions.Update(txCtx, oldTx)
		return err
	})
	return updated, err
}

func (s *Service) Delete(ctx context.Context, id, userID shared.ID) error {
	return s.uow.Do(ctx, func(txCtx context.Context) error {
		tx, err := s.transactions.FindByIDForUser(txCtx, id, userID)
		if err != nil {
			return err
		}
		if _, err := s.accounts.AdjustBalance(txCtx, tx.AccountID, userID, -balanceDelta(tx.Type, tx.Amount)); err != nil {
			return err
		}
		return s.transactions.SoftDelete(txCtx, id, userID, s.clock.Now())
	})
}

func (s *Service) List(ctx context.Context, filter Filter) ([]Transaction, error) {
	if filter.Limit <= 0 || filter.Limit > 100 {
		filter.Limit = 50
	}
	return s.transactions.List(ctx, filter)
}

func (s *Service) Get(ctx context.Context, id, userID shared.ID) (Transaction, error) {
	return s.transactions.FindByIDForUser(ctx, id, userID)
}

func (s *Service) validate(ctx context.Context, userID, accountID shared.ID, codeValue string, txType Type, amount int64, txDate time.Time) (account.Account, transactioncode.TransactionCode, error) {
	if amount <= 0 || accountID == "" || codeValue == "" || txDate.IsZero() || (txType != TypeDebit && txType != TypeCredit) {
		return account.Account{}, transactioncode.TransactionCode{}, shared.ErrValidation
	}
	acc, err := s.accounts.FindByIDForUser(ctx, accountID, userID)
	if err != nil {
		return account.Account{}, transactioncode.TransactionCode{}, err
	}
	if !acc.IsActive {
		return account.Account{}, transactioncode.TransactionCode{}, shared.ErrValidation
	}
	code, err := s.codes.FindByCode(ctx, codeValue)
	if err != nil {
		return account.Account{}, transactioncode.TransactionCode{}, err
	}
	if !code.IsActive {
		return account.Account{}, transactioncode.TransactionCode{}, shared.ErrValidation
	}
	if txType == TypeDebit && code.Type != transactioncode.CodeTypeIncome {
		return account.Account{}, transactioncode.TransactionCode{}, shared.ErrValidation
	}
	if txType == TypeCredit && code.Type != transactioncode.CodeTypeExpense {
		return account.Account{}, transactioncode.TransactionCode{}, shared.ErrValidation
	}
	return acc, code, nil
}

func balanceDelta(txType Type, amount int64) int64 {
	if txType == TypeDebit {
		return amount
	}
	return -amount
}
