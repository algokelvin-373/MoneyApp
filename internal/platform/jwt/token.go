package jwt

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"errors"
	"strings"
	"time"

	"moneyapp/internal/shared"
)

type HMACTokenManager struct {
	secret []byte
	ttl    time.Duration
}

func NewHMACTokenManager(secret string, ttl time.Duration) HMACTokenManager {
	return HMACTokenManager{secret: []byte(secret), ttl: ttl}
}

func (m HMACTokenManager) Create(userID shared.ID) (string, error) {
	exp := time.Now().UTC().Add(m.ttl).Format(time.RFC3339)
	payload := string(userID) + "|" + exp
	signature := m.sign(payload)
	return base64.RawURLEncoding.EncodeToString([]byte(payload)) + "." + signature, nil
}

func (m HMACTokenManager) Parse(token string) (shared.ID, error) {
	parts := strings.Split(token, ".")
	if len(parts) != 2 {
		return "", errors.New("invalid token")
	}
	payloadBytes, err := base64.RawURLEncoding.DecodeString(parts[0])
	if err != nil {
		return "", err
	}
	payload := string(payloadBytes)
	if !hmac.Equal([]byte(parts[1]), []byte(m.sign(payload))) {
		return "", errors.New("invalid signature")
	}
	values := strings.Split(payload, "|")
	if len(values) != 2 {
		return "", errors.New("invalid payload")
	}
	expiresAt, err := time.Parse(time.RFC3339, values[1])
	if err != nil || time.Now().UTC().After(expiresAt) {
		return "", errors.New("expired token")
	}
	return shared.ID(values[0]), nil
}

func (m HMACTokenManager) sign(payload string) string {
	mac := hmac.New(sha256.New, m.secret)
	mac.Write([]byte(payload))
	return base64.RawURLEncoding.EncodeToString(mac.Sum(nil))
}
