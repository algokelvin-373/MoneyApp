package http

import (
	"log"
	"net/http"
	"time"

	"moneyapp/internal/shared"
)

type responseMetrics struct {
	http.ResponseWriter
	status int
	bytes  int
}

func (w *responseMetrics) WriteHeader(status int) {
	if w.status != 0 {
		return
	}
	w.status = status
	w.ResponseWriter.WriteHeader(status)
}

func (w *responseMetrics) Write(body []byte) (int, error) {
	if w.status == 0 {
		w.WriteHeader(http.StatusOK)
	}
	written, err := w.ResponseWriter.Write(body)
	w.bytes += written
	return written, err
}

func accessLog(next http.Handler, logger *log.Logger) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		startedAt := time.Now()
		requestID := string(shared.NewID("req"))
		w.Header().Set("X-Request-ID", requestID)

		metrics := &responseMetrics{ResponseWriter: w}
		next.ServeHTTP(metrics, r)
		if metrics.status == 0 {
			metrics.status = http.StatusOK
		}

		logger.Printf(
			"request_id=%s method=%s path=%q status=%d bytes=%d duration=%s remote_addr=%q user_agent=%q",
			requestID,
			r.Method,
			r.URL.Path,
			metrics.status,
			metrics.bytes,
			time.Since(startedAt).Round(time.Microsecond),
			r.RemoteAddr,
			r.UserAgent(),
		)
	})
}
