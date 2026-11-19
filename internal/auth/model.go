package auth

import (
	"strings"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/shared"
)

type User struct {
	ID           shared.ID `json:"id"`
	Name         string    `json:"name"`
	Email        string    `json:"email"`
	PasswordHash string    `json:"-"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

type Repository interface {
	Create(user User) (User, error)
	FindByEmail(email string) (User, error)
	FindByID(id shared.ID) (User, error)
}

type PasswordHasher interface {
	Hash(password string) (string, error)
	Compare(hash, password string) bool
}

type TokenManager interface {
	Create(userID shared.ID) (string, error)
	Parse(token string) (shared.ID, error)
}

type Service struct {
	users    Repository
	accounts *account.Service
	hasher   PasswordHasher
	tokens   TokenManager
	clock    shared.Clock
}

func NewService(users Repository, accounts *account.Service, hasher PasswordHasher, tokens TokenManager, clock shared.Clock) *Service {
	return &Service{users: users, accounts: accounts, hasher: hasher, tokens: tokens, clock: clock}
}

type RegisterInput struct {
	Name     string `json:"name"`
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginInput struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type AuthResult struct {
	User        User   `json:"user"`
	AccessToken string `json:"access_token"`
}

func (s *Service) Register(input RegisterInput) (AuthResult, error) {
	input.Email = strings.TrimSpace(strings.ToLower(input.Email))
	input.Name = strings.TrimSpace(input.Name)
	if input.Name == "" || input.Email == "" || len(input.Password) < 8 {
		return AuthResult{}, shared.ErrValidation
	}

	hash, err := s.hasher.Hash(input.Password)
	if err != nil {
		return AuthResult{}, err
	}

	now := s.clock.Now()
	user, err := s.users.Create(User{
		ID:           shared.ID("user-" + now.Format("20060102150405.000000000")),
		Name:         input.Name,
		Email:        input.Email,
		PasswordHash: hash,
		CreatedAt:    now,
		UpdatedAt:    now,
	})
	if err != nil {
		return AuthResult{}, err
	}
	if err := s.accounts.CreateDefaultAccounts(user.ID); err != nil {
		return AuthResult{}, err
	}

	token, err := s.tokens.Create(user.ID)
	if err != nil {
		return AuthResult{}, err
	}
	return AuthResult{User: user, AccessToken: token}, nil
}

func (s *Service) Login(input LoginInput) (AuthResult, error) {
	email := strings.TrimSpace(strings.ToLower(input.Email))
	user, err := s.users.FindByEmail(email)
	if err != nil || !s.hasher.Compare(user.PasswordHash, input.Password) {
		return AuthResult{}, shared.ErrUnauthorized
	}

	token, err := s.tokens.Create(user.ID)
	if err != nil {
		return AuthResult{}, err
	}
	return AuthResult{User: user, AccessToken: token}, nil
}

func (s *Service) Authenticate(token string) (User, error) {
	userID, err := s.tokens.Parse(token)
	if err != nil {
		return User{}, shared.ErrUnauthorized
	}
	return s.users.FindByID(userID)
}
