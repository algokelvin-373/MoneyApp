package http

import (
	"bytes"
	"encoding/json"
	"fmt"
	stdhttp "net/http"
	"net/http/httptest"
	"testing"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/auth"
	"moneyapp/internal/dashboard"
	"moneyapp/internal/insight"
	"moneyapp/internal/platform/hashing"
	"moneyapp/internal/platform/jwt"
	"moneyapp/internal/repository/memory"
	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
	"moneyapp/internal/transactioncode"
)

func TestAllEndpoints(t *testing.T) {
	handler := newTestServer().Routes()

	t.Run("GET /health", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/health", "", nil)
		assertStatus(t, res, stdhttp.StatusOK)
	})

	var token string
	t.Run("POST /auth/register", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/auth/register", "", map[string]any{
			"name":     "Kelvin",
			"email":    "kelvin@example.com",
			"password": "password123",
		})
		assertStatus(t, res, stdhttp.StatusCreated)
		token = dataString(t, res, "access_token")
		if token == "" {
			t.Fatal("expected access token")
		}
	})

	t.Run("POST /auth/login", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/auth/login", "", map[string]any{
			"email":    "kelvin@example.com",
			"password": "password123",
		})
		assertStatus(t, res, stdhttp.StatusOK)
		if dataString(t, res, "access_token") == "" {
			t.Fatal("expected access token")
		}
	})

	t.Run("POST /auth/refresh", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/auth/refresh", "", nil)
		assertStatus(t, res, stdhttp.StatusOK)
	})

	t.Run("POST /auth/logout", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/auth/logout", "", nil)
		assertStatus(t, res, stdhttp.StatusOK)
	})

	var accountID string
	t.Run("GET /accounts", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/accounts", token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		items := dataSlice(t, res)
		if len(items) != 3 {
			t.Fatalf("expected 3 default accounts, got %d", len(items))
		}
		accountID = fmt.Sprint(items[0]["id"])
	})

	t.Run("GET /accounts/{id}", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/accounts/"+accountID, token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		if dataString(t, res, "id") != accountID {
			t.Fatalf("expected account id %s", accountID)
		}
	})

	t.Run("GET /transaction-codes", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/transaction-codes", token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		if got := len(dataSlice(t, res)); got != 14 {
			t.Fatalf("expected 14 transaction codes, got %d", got)
		}
	})

	t.Run("GET /transaction-codes?type=EXPENSE", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/transaction-codes?type=EXPENSE", token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		if got := len(dataSlice(t, res)); got != 9 {
			t.Fatalf("expected 9 expense codes, got %d", got)
		}
	})

	var incomeID string
	t.Run("POST /transactions debit", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/transactions", token, map[string]any{
			"account_id":       accountID,
			"code":             "SALARY",
			"transaction_type": "DEBIT",
			"amount":           100000,
			"transaction_date": "2026-08-31T10:00:00Z",
			"note":             "Gaji",
			"idempotency_key":  "salary-august",
		})
		assertStatus(t, res, stdhttp.StatusCreated)
		incomeID = dataString(t, res, "id")
		if incomeID == "" {
			t.Fatal("expected created transaction id")
		}
	})

	var expenseID string
	t.Run("POST /transactions credit", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/transactions", token, map[string]any{
			"account_id":       accountID,
			"code":             "FOOD",
			"transaction_type": "CREDIT",
			"amount":           25000,
			"transaction_date": "2026-08-31T12:00:00Z",
			"note":             "Makan siang",
			"idempotency_key":  "lunch-august-31",
		})
		assertStatus(t, res, stdhttp.StatusCreated)
		expenseID = dataString(t, res, "id")
	})

	t.Run("GET /transactions", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/transactions?account=GOPAY&type=CREDIT&code=FOOD&start_date=2026-08-01&end_date=2026-08-31", token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		if got := len(dataSlice(t, res)); got != 1 {
			t.Fatalf("expected 1 filtered transaction, got %d", got)
		}
	})

	t.Run("GET /transactions/{id}", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/transactions/"+expenseID, token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		if dataString(t, res, "id") != expenseID {
			t.Fatalf("expected transaction id %s", expenseID)
		}
	})

	t.Run("PATCH /transactions/{id}", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPatch, "/api/v1/transactions/"+expenseID, token, map[string]any{
			"account_id":       accountID,
			"code":             "TRANSPORT",
			"transaction_type": "CREDIT",
			"amount":           10000,
			"transaction_date": "2026-08-31T13:00:00Z",
			"note":             "Ojol",
		})
		assertStatus(t, res, stdhttp.StatusOK)
		if dataString(t, res, "code") != "TRANSPORT" {
			t.Fatal("expected updated transaction code")
		}
	})

	t.Run("GET /dashboard/summary", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/dashboard/summary?start_date=2026-08-01&end_date=2026-08-31", token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		data := dataMap(t, res)
		if data["total_income"].(float64) != 100000 || data["total_expense"].(float64) != 10000 {
			t.Fatalf("unexpected dashboard summary: %#v", data)
		}
	})

	t.Run("GET /insights/spending", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/insights/spending?start_date=2026-08-01&end_date=2026-08-31", token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
		data := dataMap(t, res)
		if data["total_expense"].(float64) != 10000 {
			t.Fatalf("unexpected spending summary: %#v", data)
		}
	})

	t.Run("DELETE /transactions/{id}", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodDelete, "/api/v1/transactions/"+expenseID, token, nil)
		assertStatus(t, res, stdhttp.StatusOK)
	})

	t.Run("GET /transactions/{id} deleted", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodGet, "/api/v1/transactions/"+expenseID, token, nil)
		assertStatus(t, res, stdhttp.StatusNotFound)
	})

	t.Run("POST /transactions rejects insufficient balance", func(t *testing.T) {
		res := request(t, handler, stdhttp.MethodPost, "/api/v1/transactions", token, map[string]any{
			"account_id":       accountID,
			"code":             "SHOPPING",
			"transaction_type": "CREDIT",
			"amount":           500000,
			"transaction_date": "2026-08-31T14:00:00Z",
			"note":             "Too much",
			"idempotency_key":  "overspend",
		})
		assertStatus(t, res, stdhttp.StatusBadRequest)
		if body := decodeBody(t, res); body.ErrorCode != "INSUFFICIENT_BALANCE" {
			t.Fatalf("expected INSUFFICIENT_BALANCE, got %s", body.ErrorCode)
		}
	})

	_ = incomeID
}

func TestProtectedEndpointsRequireAuthorization(t *testing.T) {
	handler := newTestServer().Routes()
	protected := []struct {
		method string
		path   string
	}{
		{stdhttp.MethodGet, "/api/v1/accounts"},
		{stdhttp.MethodGet, "/api/v1/accounts/account-id"},
		{stdhttp.MethodGet, "/api/v1/transaction-codes"},
		{stdhttp.MethodGet, "/api/v1/transactions"},
		{stdhttp.MethodGet, "/api/v1/transactions/transaction-id"},
		{stdhttp.MethodPost, "/api/v1/transactions"},
		{stdhttp.MethodPatch, "/api/v1/transactions/transaction-id"},
		{stdhttp.MethodDelete, "/api/v1/transactions/transaction-id"},
		{stdhttp.MethodGet, "/api/v1/dashboard/summary"},
		{stdhttp.MethodGet, "/api/v1/insights/spending"},
	}

	for _, item := range protected {
		t.Run(item.method+" "+item.path, func(t *testing.T) {
			res := request(t, handler, item.method, item.path, "", map[string]any{})
			assertStatus(t, res, stdhttp.StatusUnauthorized)
		})
	}
}

func newTestServer() *Server {
	clock := shared.SystemClock{}
	store := memory.NewStore(clock)
	accountRepo := memory.NewAccountRepository(store)
	codeRepo := memory.NewTransactionCodeRepository(store)
	txRepo := memory.NewTransactionRepository(store)

	accountService := account.NewService(accountRepo, clock)
	authService := auth.NewService(
		store,
		accountService,
		hashing.NewSHA256Hasher(),
		jwt.NewHMACTokenManager("test-secret", time.Hour),
		clock,
	)
	codeService := transactioncode.NewService(codeRepo)
	txService := transaction.NewService(txRepo, accountRepo, codeRepo, store.UnitOfWork(), clock)
	dashboardService := dashboard.NewService(accountRepo, txRepo)
	insightService := insight.NewService(txRepo)
	return NewServer(authService, accountService, codeService, txService, dashboardService, insightService)
}

func request(t *testing.T, handler stdhttp.Handler, method, path, token string, body any) *httptest.ResponseRecorder {
	t.Helper()
	payload := bytes.NewReader(nil)
	if body != nil {
		raw, err := json.Marshal(body)
		if err != nil {
			t.Fatal(err)
		}
		payload = bytes.NewReader(raw)
	}
	req := httptest.NewRequest(method, path, payload)
	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	res := httptest.NewRecorder()
	handler.ServeHTTP(res, req)
	return res
}

func assertStatus(t *testing.T, res *httptest.ResponseRecorder, expected int) {
	t.Helper()
	if res.Code != expected {
		t.Fatalf("expected status %d, got %d with body %s", expected, res.Code, res.Body.String())
	}
}

type testResponse struct {
	Success   bool            `json:"success"`
	Message   string          `json:"message"`
	Data      json.RawMessage `json:"data"`
	ErrorCode string          `json:"error_code"`
}

func decodeBody(t *testing.T, res *httptest.ResponseRecorder) testResponse {
	t.Helper()
	var body testResponse
	if err := json.Unmarshal(res.Body.Bytes(), &body); err != nil {
		t.Fatalf("decode response: %v, body: %s", err, res.Body.String())
	}
	return body
}

func dataMap(t *testing.T, res *httptest.ResponseRecorder) map[string]any {
	t.Helper()
	body := decodeBody(t, res)
	var data map[string]any
	if err := json.Unmarshal(body.Data, &data); err != nil {
		t.Fatalf("decode data map: %v", err)
	}
	return data
}

func dataSlice(t *testing.T, res *httptest.ResponseRecorder) []map[string]any {
	t.Helper()
	body := decodeBody(t, res)
	var data []map[string]any
	if err := json.Unmarshal(body.Data, &data); err != nil {
		t.Fatalf("decode data slice: %v", err)
	}
	return data
}

func dataString(t *testing.T, res *httptest.ResponseRecorder, key string) string {
	t.Helper()
	value, ok := dataMap(t, res)[key]
	if !ok {
		return ""
	}
	return fmt.Sprint(value)
}
