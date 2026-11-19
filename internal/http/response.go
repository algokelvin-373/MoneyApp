package http

import (
	"encoding/json"
	stdhttp "net/http"

	"moneyapp/internal/shared"
)

type Response struct {
	Success   bool        `json:"success"`
	Message   string      `json:"message"`
	Data      interface{} `json:"data,omitempty"`
	ErrorCode string      `json:"error_code,omitempty"`
}

func WriteSuccess(w stdhttp.ResponseWriter, status int, message string, data interface{}) {
	writeJSON(w, status, Response{Success: true, Message: message, Data: data})
}

func WriteError(w stdhttp.ResponseWriter, err error) {
	status := stdhttp.StatusInternalServerError
	code := "INTERNAL_ERROR"
	message := "Internal server error"
	switch err {
	case shared.ErrValidation:
		status, code, message = stdhttp.StatusBadRequest, "VALIDATION_ERROR", "Validation failed"
	case shared.ErrUnauthorized:
		status, code, message = stdhttp.StatusUnauthorized, "UNAUTHORIZED", "Unauthorized"
	case shared.ErrForbidden:
		status, code, message = stdhttp.StatusForbidden, "FORBIDDEN", "Forbidden"
	case shared.ErrNotFound:
		status, code, message = stdhttp.StatusNotFound, "NOT_FOUND", "Data not found"
	case shared.ErrDuplicate:
		status, code, message = stdhttp.StatusConflict, "DUPLICATE_DATA", "Duplicate data"
	case shared.ErrInsufficientBalance:
		status, code, message = stdhttp.StatusBadRequest, "INSUFFICIENT_BALANCE", "Insufficient account balance"
	}
	writeJSON(w, status, Response{Success: false, Message: message, ErrorCode: code})
}

func writeJSON(w stdhttp.ResponseWriter, status int, body Response) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(body)
}
