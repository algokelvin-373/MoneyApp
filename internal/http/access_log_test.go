package http

import (
	"bytes"
	"log"
	stdhttp "net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestAccessLogRecordsRequestMetrics(t *testing.T) {
	var output bytes.Buffer
	logger := log.New(&output, "", 0)
	handler := accessLog(stdhttp.HandlerFunc(func(w stdhttp.ResponseWriter, _ *stdhttp.Request) {
		w.WriteHeader(stdhttp.StatusCreated)
		_, _ = w.Write([]byte("created"))
	}), logger)

	request := httptest.NewRequest(stdhttp.MethodPost, "/api/v1/transactions?type=EXPENSE", nil)
	request.Header.Set("Authorization", "Bearer secret-token")
	request.Header.Set("User-Agent", "MoneyApp-Test")
	response := httptest.NewRecorder()
	handler.ServeHTTP(response, request)

	line := output.String()
	for _, expected := range []string{
		"request_id=req-",
		"method=POST",
		`path="/api/v1/transactions"`,
		"status=201",
		"bytes=7",
		`user_agent="MoneyApp-Test"`,
	} {
		if !strings.Contains(line, expected) {
			t.Fatalf("expected log to contain %q, got %q", expected, line)
		}
	}
	if strings.Contains(line, "secret-token") || strings.Contains(line, "type=EXPENSE") {
		t.Fatalf("access log exposed sensitive header or query string: %q", line)
	}
	if requestID := response.Header().Get("X-Request-ID"); !strings.HasPrefix(requestID, "req-") {
		t.Fatalf("expected response request ID, got %q", requestID)
	}
}
