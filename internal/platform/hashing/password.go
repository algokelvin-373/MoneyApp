package hashing

import (
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"strings"
)

type SHA256Hasher struct{}

func NewSHA256Hasher() SHA256Hasher {
	return SHA256Hasher{}
}

func (SHA256Hasher) Hash(password string) (string, error) {
	salt := make([]byte, 16)
	if _, err := rand.Read(salt); err != nil {
		return "", err
	}
	sum := sha256.Sum256(append(salt, []byte(password)...))
	return base64.RawURLEncoding.EncodeToString(salt) + "." + base64.RawURLEncoding.EncodeToString(sum[:]), nil
}

func (SHA256Hasher) Compare(hash, password string) bool {
	parts := strings.Split(hash, ".")
	if len(parts) != 2 {
		return false
	}
	salt, err := base64.RawURLEncoding.DecodeString(parts[0])
	if err != nil {
		return false
	}
	expected := sha256.Sum256(append(salt, []byte(password)...))
	return parts[1] == base64.RawURLEncoding.EncodeToString(expected[:])
}
