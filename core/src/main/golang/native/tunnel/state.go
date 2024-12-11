package tunnel

import (
	"github.com/Dreamacro/foss/tunnel"
)

func QueryMode() string {
	return tunnel.Mode().String()
}
