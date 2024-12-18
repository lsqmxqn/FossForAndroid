package tunnel

import (
	"context"
	"net"
	"strings"

	"github.com/Dreamacro/foss/component/dialer"
	C "github.com/Dreamacro/foss/constant"
	CTX "github.com/Dreamacro/foss/context"
	"github.com/Dreamacro/foss/tunnel"
)

func init() {
	dialer.DefaultTunnelDialer = func(context context.Context, network, address string) (net.Conn, error) {
		if !strings.HasPrefix(network, "tcp") {
			return nil, net.UnknownNetworkError("unsupported network")
		}

		host, port, err := net.SplitHostPort(address)
		if err != nil {
			return nil, err
		}

		left, right := net.Pipe()

		metadata := &C.Metadata{
			NetWork:    C.TCP,
			Type:       C.HTTPCONNECT,
			SrcIP:      loopback,
			SrcPort:    "65535",
			DstPort:    port,
			AddrType:   C.AtypDomainName,
			Host:       host,
			RawSrcAddr: left.RemoteAddr(),
			RawDstAddr: left.LocalAddr(),
		}

		tunnel.TCPIn() <- CTX.NewConnContext(right, metadata)

		return left, nil
	}
}
