package shared

import "errors"

var (
	ErrUnauthorized        = errors.New("unauthorized")
	ErrForbidden           = errors.New("forbidden")
	ErrNotFound            = errors.New("not found")
	ErrValidation          = errors.New("validation failed")
	ErrDuplicate           = errors.New("duplicate data")
	ErrInsufficientBalance = errors.New("insufficient account balance")
)
