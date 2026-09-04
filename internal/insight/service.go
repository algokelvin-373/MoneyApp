package insight

import (
	"context"
	"time"

	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
)

type SpendingCategory struct {
	Code       string  `json:"code"`
	Amount     int64   `json:"amount"`
	Percentage float64 `json:"percentage"`
}

type SpendingSummary struct {
	TotalExpense int64              `json:"total_expense"`
	Categories   []SpendingCategory `json:"categories"`
}

type Service struct {
	transactions transaction.Repository
}

func NewService(transactions transaction.Repository) *Service {
	return &Service{transactions: transactions}
}

func (s *Service) Spending(ctx context.Context, userID shared.ID, startDate, endDate *time.Time) (SpendingSummary, error) {
	txs, err := s.transactions.List(ctx, transaction.Filter{
		UserID:    userID,
		Type:      transaction.TypeCredit,
		StartDate: startDate,
		EndDate:   endDate,
		Limit:     100,
	})
	if err != nil {
		return SpendingSummary{}, err
	}
	byCode := map[string]int64{}
	var total int64
	for _, tx := range txs {
		byCode[tx.Code] += tx.Amount
		total += tx.Amount
	}
	result := SpendingSummary{TotalExpense: total}
	for code, amount := range byCode {
		percentage := 0.0
		if total > 0 {
			percentage = float64(amount) / float64(total) * 100
		}
		result.Categories = append(result.Categories, SpendingCategory{Code: code, Amount: amount, Percentage: percentage})
	}
	return result, nil
}
