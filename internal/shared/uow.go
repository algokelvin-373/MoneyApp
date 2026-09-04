package shared

import "context"

type UnitOfWorkFunc struct{}

func (u UnitOfWorkFunc) Do(ctx context.Context, fn func(context.Context) error) error {
	return fn(ctx)
}
