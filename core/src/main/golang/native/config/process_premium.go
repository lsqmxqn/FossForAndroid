//go:build premium

package config

import "github.com/Dreamacro/foss/config"

func patchTun(cfg *config.RawConfig, _ string) error {
	cfg.Tun.Enable = false

	return nil
}
