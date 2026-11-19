package shared

import (
	"strconv"
	"sync/atomic"
	"time"
)

var sequence uint64

func NewID(prefix string) ID {
	n := atomic.AddUint64(&sequence, 1)
	return ID(prefix + "-" + strconv.FormatInt(time.Now().UTC().UnixNano(), 36) + "-" + strconv.FormatUint(n, 36))
}
