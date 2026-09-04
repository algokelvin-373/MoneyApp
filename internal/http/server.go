package http

import (
	"encoding/json"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/auth"
	"moneyapp/internal/dashboard"
	"moneyapp/internal/insight"
	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
	"moneyapp/internal/transactioncode"
)

type Server struct {
	auth      *auth.Service
	accounts  *account.Service
	codes     *transactioncode.Service
	txs       *transaction.Service
	dashboard *dashboard.Service
	insights  *insight.Service
}

func NewServer(authService *auth.Service, accountService *account.Service, codeService *transactioncode.Service, txService *transaction.Service, dashboardService *dashboard.Service, insightService *insight.Service) *Server {
	return &Server{auth: authService, accounts: accountService, codes: codeService, txs: txService, dashboard: dashboardService, insights: insightService}
}

func (s *Server) Routes() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("POST /api/v1/auth/register", s.register)
	mux.HandleFunc("POST /api/v1/auth/login", s.login)
	mux.HandleFunc("POST /api/v1/auth/refresh", s.refresh)
	mux.HandleFunc("POST /api/v1/auth/logout", s.logout)
	mux.HandleFunc("GET /api/v1/accounts", s.withAuth(s.listAccounts))
	mux.HandleFunc("GET /api/v1/accounts/{id}", s.withAuth(s.getAccount))
	mux.HandleFunc("GET /api/v1/transaction-codes", s.withAuth(s.listCodes))
	mux.HandleFunc("GET /api/v1/transactions", s.withAuth(s.listTransactions))
	mux.HandleFunc("GET /api/v1/transactions/{id}", s.withAuth(s.getTransaction))
	mux.HandleFunc("POST /api/v1/transactions", s.withAuth(s.createTransaction))
	mux.HandleFunc("PATCH /api/v1/transactions/{id}", s.withAuth(s.updateTransaction))
	mux.HandleFunc("DELETE /api/v1/transactions/{id}", s.withAuth(s.deleteTransaction))
	mux.HandleFunc("GET /api/v1/dashboard/summary", s.withAuth(s.dashboardSummary))
	mux.HandleFunc("GET /api/v1/insights/spending", s.withAuth(s.spendingInsight))
	mux.HandleFunc("GET /api/v1/health", func(w http.ResponseWriter, r *http.Request) {
		WriteSuccess(w, http.StatusOK, "API is healthy", map[string]string{"status": "ok"})
	})
	return accessLog(mux, log.Default())
}

func (s *Server) register(w http.ResponseWriter, r *http.Request) {
	var input auth.RegisterInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		WriteError(w, shared.ErrValidation)
		return
	}
	result, err := s.auth.Register(r.Context(), input)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusCreated, "Registration successful", result)
}

func (s *Server) login(w http.ResponseWriter, r *http.Request) {
	var input auth.LoginInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		WriteError(w, shared.ErrValidation)
		return
	}
	result, err := s.auth.Login(r.Context(), input)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Login successful", result)
}

func (s *Server) refresh(w http.ResponseWriter, r *http.Request) {
	WriteSuccess(w, http.StatusOK, "Refresh token endpoint reserved for persistent token store", map[string]string{})
}

func (s *Server) logout(w http.ResponseWriter, r *http.Request) {
	WriteSuccess(w, http.StatusOK, "Logout successful", map[string]string{})
}

func (s *Server) listAccounts(w http.ResponseWriter, r *http.Request, user auth.User) {
	items, err := s.accounts.List(r.Context(), user.ID)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Accounts retrieved successfully", items)
}

func (s *Server) getAccount(w http.ResponseWriter, r *http.Request, user auth.User) {
	item, err := s.accounts.Get(r.Context(), shared.ID(r.PathValue("id")), user.ID)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Account retrieved successfully", item)
}

func (s *Server) listCodes(w http.ResponseWriter, r *http.Request, user auth.User) {
	var codeType *transactioncode.CodeType
	if raw := r.URL.Query().Get("type"); raw != "" {
		value := transactioncode.CodeType(raw)
		codeType = &value
	}
	items, err := s.codes.List(r.Context(), codeType)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Transaction codes retrieved successfully", items)
}

func (s *Server) listTransactions(w http.ResponseWriter, r *http.Request, user auth.User) {
	filter := transaction.Filter{
		UserID:  user.ID,
		Account: r.URL.Query().Get("account"),
		Code:    r.URL.Query().Get("code"),
		Type:    transaction.Type(r.URL.Query().Get("type")),
		Limit:   parseInt(r.URL.Query().Get("limit"), 50),
		Offset:  parseInt(r.URL.Query().Get("offset"), 0),
	}
	filter.StartDate = parseDatePtr(r.URL.Query().Get("start_date"))
	filter.EndDate = parseEndDatePtr(r.URL.Query().Get("end_date"))
	items, err := s.txs.List(r.Context(), filter)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Transactions retrieved successfully", items)
}

func (s *Server) getTransaction(w http.ResponseWriter, r *http.Request, user auth.User) {
	item, err := s.txs.Get(r.Context(), shared.ID(r.PathValue("id")), user.ID)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Transaction retrieved successfully", item)
}

func (s *Server) createTransaction(w http.ResponseWriter, r *http.Request, user auth.User) {
	var input transaction.CreateInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		WriteError(w, shared.ErrValidation)
		return
	}
	input.UserID = user.ID
	item, err := s.txs.Create(r.Context(), input)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusCreated, "Transaction created successfully", item)
}

func (s *Server) updateTransaction(w http.ResponseWriter, r *http.Request, user auth.User) {
	var input transaction.UpdateInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		WriteError(w, shared.ErrValidation)
		return
	}
	input.UserID = user.ID
	input.ID = shared.ID(r.PathValue("id"))
	item, err := s.txs.Update(r.Context(), input)
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Transaction updated successfully", item)
}

func (s *Server) deleteTransaction(w http.ResponseWriter, r *http.Request, user auth.User) {
	if err := s.txs.Delete(r.Context(), shared.ID(r.PathValue("id")), user.ID); err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Transaction deleted successfully", map[string]string{})
}

func (s *Server) dashboardSummary(w http.ResponseWriter, r *http.Request, user auth.User) {
	result, err := s.dashboard.Summary(r.Context(), user.ID, parseDatePtr(r.URL.Query().Get("start_date")), parseEndDatePtr(r.URL.Query().Get("end_date")))
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Dashboard summary retrieved successfully", result)
}

func (s *Server) spendingInsight(w http.ResponseWriter, r *http.Request, user auth.User) {
	result, err := s.insights.Spending(r.Context(), user.ID, parseDatePtr(r.URL.Query().Get("start_date")), parseEndDatePtr(r.URL.Query().Get("end_date")))
	if err != nil {
		WriteError(w, err)
		return
	}
	WriteSuccess(w, http.StatusOK, "Spending insight retrieved successfully", result)
}

func (s *Server) withAuth(next func(http.ResponseWriter, *http.Request, auth.User)) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		token := strings.TrimPrefix(r.Header.Get("Authorization"), "Bearer ")
		user, err := s.auth.Authenticate(r.Context(), token)
		if err != nil {
			WriteError(w, shared.ErrUnauthorized)
			return
		}
		next(w, r, user)
	}
}

func parseDatePtr(value string) *time.Time {
	if value == "" {
		return nil
	}
	parsed, err := time.Parse("2006-01-02", value)
	if err != nil {
		return nil
	}
	return &parsed
}

func parseEndDatePtr(value string) *time.Time {
	parsed := parseDatePtr(value)
	if parsed == nil {
		return nil
	}
	endOfDay := parsed.Add(24*time.Hour - time.Nanosecond)
	return &endOfDay
}

func parseInt(value string, fallback int) int {
	if value == "" {
		return fallback
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}
	return parsed
}
