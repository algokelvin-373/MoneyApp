package memory

import (
	"sort"
	"sync"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/auth"
	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
	"moneyapp/internal/transactioncode"
)

type Store struct {
	mu           sync.Mutex
	users        map[shared.ID]auth.User
	userByEmail  map[string]shared.ID
	accounts     map[shared.ID]account.Account
	codes        map[string]transactioncode.TransactionCode
	transactions map[shared.ID]transaction.Transaction
}

func NewStore(clock shared.Clock) *Store {
	codes := map[string]transactioncode.TransactionCode{}
	for _, item := range transactioncode.SeedCodes(clock.Now()) {
		codes[item.Code] = item
	}
	return &Store{
		users:        map[shared.ID]auth.User{},
		userByEmail:  map[string]shared.ID{},
		accounts:     map[shared.ID]account.Account{},
		codes:        codes,
		transactions: map[shared.ID]transaction.Transaction{},
	}
}

func (s *Store) UnitOfWork() shared.UnitOfWorkFunc {
	return shared.UnitOfWorkFunc{}
}

func (s *Store) Create(user auth.User) (auth.User, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if _, exists := s.userByEmail[user.Email]; exists {
		return auth.User{}, shared.ErrDuplicate
	}
	user.ID = shared.NewID("user")
	s.users[user.ID] = user
	s.userByEmail[user.Email] = user.ID
	return user, nil
}

func (s *Store) FindByEmail(email string) (auth.User, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	id, ok := s.userByEmail[email]
	if !ok {
		return auth.User{}, shared.ErrNotFound
	}
	return s.users[id], nil
}

func (s *Store) FindByID(id shared.ID) (auth.User, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	user, ok := s.users[id]
	if !ok {
		return auth.User{}, shared.ErrNotFound
	}
	return user, nil
}

var _ auth.Repository = (*Store)(nil)
var _ account.Repository = (*AccountRepository)(nil)
var _ transactioncode.Repository = (*TransactionCodeRepository)(nil)
var _ transaction.Repository = (*TransactionRepository)(nil)

type AccountRepository struct{ store *Store }

func NewAccountRepository(store *Store) account.Repository {
	return AccountRepository{store: store}
}

func (r AccountRepository) ListByUser(userID shared.ID) ([]account.Account, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	var result []account.Account
	for _, item := range r.store.accounts {
		if item.UserID == userID {
			result = append(result, item)
		}
	}
	sort.Slice(result, func(i, j int) bool { return result[i].Code < result[j].Code })
	return result, nil
}

func (r AccountRepository) FindByIDForUser(id, userID shared.ID) (account.Account, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	return r.findByIDForUser(id, userID)
}

func (r AccountRepository) FindByCodeForUser(code string, userID shared.ID) (account.Account, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	for _, item := range r.store.accounts {
		if item.UserID == userID && item.Code == code {
			return item, nil
		}
	}
	return account.Account{}, shared.ErrNotFound
}

func (r AccountRepository) Create(item account.Account) (account.Account, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	item.ID = shared.NewID("account")
	r.store.accounts[item.ID] = item
	return item, nil
}

func (r AccountRepository) Update(item account.Account) (account.Account, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	if _, ok := r.store.accounts[item.ID]; !ok {
		return account.Account{}, shared.ErrNotFound
	}
	r.store.accounts[item.ID] = item
	return item, nil
}

func (r AccountRepository) AdjustBalance(id, userID shared.ID, delta int64) (account.Account, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	item, err := r.findByIDForUser(id, userID)
	if err != nil {
		return account.Account{}, err
	}
	item.Balance += delta
	item.UpdatedAt = time.Now().UTC()
	r.store.accounts[item.ID] = item
	return item, nil
}

func (r AccountRepository) findByIDForUser(id, userID shared.ID) (account.Account, error) {
	item, ok := r.store.accounts[id]
	if !ok || item.UserID != userID {
		return account.Account{}, shared.ErrNotFound
	}
	return item, nil
}

type TransactionCodeRepository struct{ store *Store }

func NewTransactionCodeRepository(store *Store) transactioncode.Repository {
	return TransactionCodeRepository{store: store}
}

func (r TransactionCodeRepository) List(codeType *transactioncode.CodeType) ([]transactioncode.TransactionCode, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	var result []transactioncode.TransactionCode
	for _, item := range r.store.codes {
		if codeType == nil || item.Type == *codeType {
			result = append(result, item)
		}
	}
	sort.Slice(result, func(i, j int) bool { return result[i].Code < result[j].Code })
	return result, nil
}

func (r TransactionCodeRepository) FindByID(id shared.ID) (transactioncode.TransactionCode, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	for _, item := range r.store.codes {
		if item.ID == id {
			return item, nil
		}
	}
	return transactioncode.TransactionCode{}, shared.ErrNotFound
}

func (r TransactionCodeRepository) FindByCode(code string) (transactioncode.TransactionCode, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	item, ok := r.store.codes[code]
	if !ok {
		return transactioncode.TransactionCode{}, shared.ErrNotFound
	}
	return item, nil
}

type TransactionRepository struct{ store *Store }

func NewTransactionRepository(store *Store) transaction.Repository {
	return TransactionRepository{store: store}
}

func (r TransactionRepository) Create(tx transaction.Transaction) (transaction.Transaction, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	r.store.transactions[tx.ID] = tx
	return tx, nil
}

func (r TransactionRepository) Update(tx transaction.Transaction) (transaction.Transaction, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	if _, ok := r.store.transactions[tx.ID]; !ok {
		return transaction.Transaction{}, shared.ErrNotFound
	}
	r.store.transactions[tx.ID] = tx
	return tx, nil
}

func (r TransactionRepository) FindByIDForUser(id, userID shared.ID) (transaction.Transaction, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	tx, ok := r.store.transactions[id]
	if !ok || tx.UserID != userID || tx.DeletedAt != nil {
		return transaction.Transaction{}, shared.ErrNotFound
	}
	return tx, nil
}

func (r TransactionRepository) FindByIdempotencyKey(userID shared.ID, key string) (transaction.Transaction, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	if key == "" {
		return transaction.Transaction{}, shared.ErrNotFound
	}
	for _, tx := range r.store.transactions {
		if tx.UserID == userID && tx.IdempotencyKey == key && tx.DeletedAt == nil {
			return tx, nil
		}
	}
	return transaction.Transaction{}, shared.ErrNotFound
}

func (r TransactionRepository) List(filter transaction.Filter) ([]transaction.Transaction, error) {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	var result []transaction.Transaction
	for _, tx := range r.store.transactions {
		if tx.UserID != filter.UserID || tx.DeletedAt != nil {
			continue
		}
		if filter.Account != "" && tx.AccountCode != filter.Account {
			continue
		}
		if filter.Code != "" && tx.Code != filter.Code {
			continue
		}
		if filter.Type != "" && tx.Type != filter.Type {
			continue
		}
		if filter.StartDate != nil && tx.TransactionDate.Before(*filter.StartDate) {
			continue
		}
		if filter.EndDate != nil && tx.TransactionDate.After(*filter.EndDate) {
			continue
		}
		result = append(result, tx)
	}
	sort.Slice(result, func(i, j int) bool { return result[i].TransactionDate.After(result[j].TransactionDate) })
	if filter.Offset > len(result) {
		return []transaction.Transaction{}, nil
	}
	end := filter.Offset + filter.Limit
	if end > len(result) {
		end = len(result)
	}
	return result[filter.Offset:end], nil
}

func (r TransactionRepository) SoftDelete(id, userID shared.ID, deletedAt time.Time) error {
	r.store.mu.Lock()
	defer r.store.mu.Unlock()
	tx, ok := r.store.transactions[id]
	if !ok || tx.UserID != userID || tx.DeletedAt != nil {
		return shared.ErrNotFound
	}
	tx.DeletedAt = &deletedAt
	tx.UpdatedAt = deletedAt
	r.store.transactions[id] = tx
	return nil
}
