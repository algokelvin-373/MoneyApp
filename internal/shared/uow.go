package shared

type UnitOfWorkFunc struct{}

func (u UnitOfWorkFunc) Do(fn func() error) error {
	return fn()
}
