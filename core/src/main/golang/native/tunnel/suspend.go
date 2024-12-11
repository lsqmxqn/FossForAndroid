package tunnel

import "github.com/Dreamacro/foss/adapter/provider"

func Suspend(s bool) {
	provider.Suspend(s)
}
