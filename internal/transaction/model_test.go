package transaction

import (
	"testing"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/shared"
	"moneyapp/internal/transactioncode"
)

func TestCreateCreditRejectsNegativeBalance(t *testing.T) {
	store := newMemoryFixture()
	userID := shared.ID("user-1")
	acc, _ := store.accounts.Create(account.Account{ID: "acc-1", UserID: userID, Code: "GOPAY", Name: "GoPay", IsActive: true})
	store.codes.items["FOOD"] = transactioncode.TransactionCode{ID: "code-food", Code: "FOOD", Type: transactioncode.CodeTypeExpense, IsActive: true}
	service := NewService(store.transactions, store.accounts, store.codes, noopUOW{}, fixedClock{})

	_, err := service.Create(CreateInput{
		UserID:          userID,
		AccountID:       acc.ID,
		Code:            "FOOD",
		Type:            TypeCredit,
		Amount:          1000,
		TransactionDate: time.Now(),
	})

	if err != shared.ErrInsufficientBalance {
		t.Fatalf("expected insufficient balance, got %v", err)
	}
}

type fixedClock struct{}

func (fixedClock) Now() time.Time {
	return time.Date(2026, 8, 31, 0, 0, 0, 0, time.UTC)
}

type noopUOW struct{}

func (noopUOW) Do(fn func() error) error { return fn() }

type memoryFixture struct {
	accounts     *memoryAccountRepo
	codes        *memoryCodeRepo
	transactions *memoryTxRepo
}

func newMemoryFixture() memoryFixture {
	return memoryFixture{accounts: &memoryAccountRepo{items: map[shared.ID]account.Account{}}, codes: &memoryCodeRepo{items: map[string]transactioncode.TransactionCode{}}, transactions: &memoryTxRepo{items: map[shared.ID]Transaction{}}}
}

type memoryAccountRepo struct{ items map[shared.ID]account.Account }

func (r *memoryAccountRepo) ListByUser(userID shared.ID) ([]account.Account, error) { return nil, nil }
func (r *memoryAccountRepo) FindByIDForUser(id, userID shared.ID) (account.Account, error) {
	item, ok := r.items[id]
	if !ok || item.UserID != userID {
		return account.Account{}, shared.ErrNotFound
	}
	return item, nil
}
func (r *memoryAccountRepo) FindByCodeForUser(code string, userID shared.ID) (account.Account, error) {
	return account.Account{}, shared.ErrNotFound
}
func (r *memoryAccountRepo) Create(item account.Account) (account.Account, error) {
	r.items[item.ID] = item
	return item, nil
}
func (r *memoryAccountRepo) Update(item account.Account) (account.Account, error) {
	r.items[item.ID] = item
	return item, nil
}
func (r *memoryAccountRepo) AdjustBalance(id, userID shared.ID, delta int64) (account.Account, error) {
	item, err := r.FindByIDForUser(id, userID)
	if err != nil {
		return account.Account{}, err
	}
	item.Balance += delta
	r.items[id] = item
	return item, nil
}

type memoryCodeRepo struct {
	items map[string]transactioncode.TransactionCode
}

func (r *memoryCodeRepo) List(codeType *transactioncode.CodeType) ([]transactioncode.TransactionCode, error) {
	return nil, nil
}
func (r *memoryCodeRepo) FindByID(id shared.ID) (transactioncode.TransactionCode, error) {
	return transactioncode.TransactionCode{}, shared.ErrNotFound
}
func (r *memoryCodeRepo) FindByCode(code string) (transactioncode.TransactionCode, error) {
	item, ok := r.items[code]
	if !ok {
		return transactioncode.TransactionCode{}, shared.ErrNotFound
	}
	return item, nil
}

type memoryTxRepo struct{ items map[shared.ID]Transaction }

func (r *memoryTxRepo) Create(tx Transaction) (Transaction, error) {
	r.items[tx.ID] = tx
	return tx, nil
}
func (r *memoryTxRepo) Update(tx Transaction) (Transaction, error) {
	r.items[tx.ID] = tx
	return tx, nil
}
func (r *memoryTxRepo) FindByIDForUser(id, userID shared.ID) (Transaction, error) {
	return Transaction{}, shared.ErrNotFound
}
func (r *memoryTxRepo) FindByIdempotencyKey(userID shared.ID, key string) (Transaction, error) {
	return Transaction{}, shared.ErrNotFound
}
func (r *memoryTxRepo) List(filter Filter) ([]Transaction, error)                  { return nil, nil }
func (r *memoryTxRepo) SoftDelete(id, userID shared.ID, deletedAt time.Time) error { return nil }
