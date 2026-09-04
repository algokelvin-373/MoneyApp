package dashboard

import (
	"context"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
)

type Summary struct {
	TotalBalance       int64                     `json:"total_balance"`
	TotalIncome        int64                     `json:"total_income"`
	TotalExpense       int64                     `json:"total_expense"`
	CashFlow           int64                     `json:"cash_flow"`
	Accounts           []account.Account         `json:"accounts"`
	RecentTransactions []transaction.Transaction `json:"recent_transactions"`
}

type Service struct {
	accounts     account.Repository
	transactions transaction.Repository
}

func NewService(accounts account.Repository, transactions transaction.Repository) *Service {
	return &Service{accounts: accounts, transactions: transactions}
}

func (s *Service) Summary(ctx context.Context, userID shared.ID, startDate, endDate *time.Time) (Summary, error) {
	accounts, err := s.accounts.ListByUser(ctx, userID)
	if err != nil {
		return Summary{}, err
	}
	result := Summary{Accounts: accounts}
	for _, acc := range accounts {
		if acc.IsActive {
			result.TotalBalance += acc.Balance
		}
	}
	txs, err := s.transactions.List(ctx, transaction.Filter{UserID: userID, StartDate: startDate, EndDate: endDate, Limit: 100})
	if err != nil {
		return Summary{}, err
	}
	for _, tx := range txs {
		if tx.Type == transaction.TypeDebit {
			result.TotalIncome += tx.Amount
		}
		if tx.Type == transaction.TypeCredit {
			result.TotalExpense += tx.Amount
		}
	}
	result.CashFlow = result.TotalIncome - result.TotalExpense
	if len(txs) > 5 {
		txs = txs[:5]
	}
	result.RecentTransactions = txs
	return result, nil
}
