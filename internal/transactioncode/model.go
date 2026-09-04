package transactioncode

import (
	"context"
	"time"

	"moneyapp/internal/shared"
)

type CodeType string

const (
	CodeTypeIncome  CodeType = "INCOME"
	CodeTypeExpense CodeType = "EXPENSE"
)

type TransactionCode struct {
	ID        shared.ID `json:"id"`
	Code      string    `json:"code"`
	Name      string    `json:"name"`
	Type      CodeType  `json:"type"`
	IsActive  bool      `json:"is_active"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type Repository interface {
	List(ctx context.Context, codeType *CodeType) ([]TransactionCode, error)
	FindByID(ctx context.Context, id shared.ID) (TransactionCode, error)
	FindByCode(ctx context.Context, code string) (TransactionCode, error)
}

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) List(ctx context.Context, codeType *CodeType) ([]TransactionCode, error) {
	return s.repo.List(ctx, codeType)
}

func SeedCodes(now time.Time) []TransactionCode {
	items := []TransactionCode{
		{ID: "code-food", Code: "FOOD", Name: "Makanan & Minuman", Type: CodeTypeExpense},
		{ID: "code-transport", Code: "TRANSPORT", Name: "Transportasi", Type: CodeTypeExpense},
		{ID: "code-shopping", Code: "SHOPPING", Name: "Belanja", Type: CodeTypeExpense},
		{ID: "code-bill", Code: "BILL", Name: "Tagihan", Type: CodeTypeExpense},
		{ID: "code-entertainment", Code: "ENTERTAINMENT", Name: "Hiburan", Type: CodeTypeExpense},
		{ID: "code-health", Code: "HEALTH", Name: "Kesehatan", Type: CodeTypeExpense},
		{ID: "code-education", Code: "EDUCATION", Name: "Pendidikan", Type: CodeTypeExpense},
		{ID: "code-social", Code: "SOCIAL", Name: "Sosial", Type: CodeTypeExpense},
		{ID: "code-other-expense", Code: "OTHER_EXPENSE", Name: "Pengeluaran Lainnya", Type: CodeTypeExpense},
		{ID: "code-salary", Code: "SALARY", Name: "Gaji", Type: CodeTypeIncome},
		{ID: "code-bonus", Code: "BONUS", Name: "Bonus", Type: CodeTypeIncome},
		{ID: "code-refund", Code: "REFUND", Name: "Refund", Type: CodeTypeIncome},
		{ID: "code-cashback", Code: "CASHBACK", Name: "Cashback", Type: CodeTypeIncome},
		{ID: "code-other-income", Code: "OTHER_INCOME", Name: "Pemasukan Lainnya", Type: CodeTypeIncome},
	}
	for i := range items {
		items[i].IsActive = true
		items[i].CreatedAt = now
		items[i].UpdatedAt = now
	}
	return items
}
