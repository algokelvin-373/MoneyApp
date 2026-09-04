package main

import (
	"context"
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
	"moneyapp/internal/repository/postgres"
	"moneyapp/internal/shared"
	"moneyapp/internal/transaction"
	"moneyapp/internal/transactioncode"
)

func main() {
	log.SetOutput(os.Stdout)
	log.SetFlags(log.Ldate | log.Ltime | log.Lmicroseconds | log.LUTC)

	clock := shared.SystemClock{}
	var userRepo auth.Repository
	var accountRepo account.Repository
	var codeRepo transactioncode.Repository
	var txRepo transaction.Repository
	var unitOfWork transaction.UnitOfWork

	databaseURL := os.Getenv("DATABASE_URL")
	if databaseURL == "" {
		store := memory.NewStore(clock)
		userRepo = store
		accountRepo = memory.NewAccountRepository(store)
		codeRepo = memory.NewTransactionCodeRepository(store)
		txRepo = memory.NewTransactionRepository(store)
		unitOfWork = store.UnitOfWork()
		log.Print("repository=memory (set DATABASE_URL to enable PostgreSQL)")
	} else {
		ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
		defer cancel()
		store, err := postgres.NewStore(ctx, databaseURL)
		if err != nil {
			log.Fatalf("connect PostgreSQL: %v", err)
		}
		defer store.Close()
		if err := store.Migrate(ctx); err != nil {
			log.Fatalf("migrate PostgreSQL: %v", err)
		}
		userRepo = store
		accountRepo = postgres.NewAccountRepository(store)
		codeRepo = postgres.NewTransactionCodeRepository(store)
		txRepo = postgres.NewTransactionRepository(store)
		unitOfWork = store.UnitOfWork()
		log.Print("repository=postgres connected and migrated")
	}

	accountService := account.NewService(accountRepo, clock)
	authService := auth.NewService(userRepo, accountService, hashing.NewSHA256Hasher(), jwt.NewHMACTokenManager(env("JWT_SECRET", "dev-secret-change-me"), 24*time.Hour), clock)
	codeService := transactioncode.NewService(codeRepo)
	txService := transaction.NewService(txRepo, accountRepo, codeRepo, unitOfWork, clock)
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
