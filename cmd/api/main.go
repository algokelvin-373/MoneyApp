package main

import (
	"log"
	"net/http"
	"os"
	"time"

	"moneyapp/internal/account"
	"moneyapp/internal/auth"
	"moneyapp/internal/dashboard"
	apphttp "moneyapp/internal/http"
	"moneyapp/internal/insight"
	"moneyapp/internal/platform/hashing"
	"moneyapp/internal/platform/jwt"
	"moneyapp/internal/repository/memory"
	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
	"moneyapp/internal/transactioncode"
)

func main() {
	log.SetOutput(os.Stdout)
	log.SetFlags(log.Ldate | log.Ltime | log.Lmicroseconds | log.LUTC)

	clock := shared.SystemClock{}
	store := memory.NewStore(clock)
	accountRepo := memory.NewAccountRepository(store)
	codeRepo := memory.NewTransactionCodeRepository(store)
	txRepo := memory.NewTransactionRepository(store)

	accountService := account.NewService(accountRepo, clock)
	authService := auth.NewService(store, accountService, hashing.NewSHA256Hasher(), jwt.NewHMACTokenManager(env("JWT_SECRET", "dev-secret-change-me"), 24*time.Hour), clock)
	codeService := transactioncode.NewService(codeRepo)
	txService := transaction.NewService(txRepo, accountRepo, codeRepo, store.UnitOfWork(), clock)
	dashboardService := dashboard.NewService(accountRepo, txRepo)
	insightService := insight.NewService(txRepo)
	server := apphttp.NewServer(authService, accountService, codeService, txService, dashboardService, insightService)

	addr := env("HTTP_ADDR", ":8080")
	log.Printf("MoneyApp API running on %s", addr)
	if err := http.ListenAndServe(addr, server.Routes()); err != nil {
		log.Fatal(err)
	}
}

func env(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}
