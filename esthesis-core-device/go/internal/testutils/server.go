package testutils

import (
	"encoding/json"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/appConstants"
	"github.com/esthesis-iot/esthesis-device/internal/pkg/dto"
	"net"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

// RouteHandler defines how to handle a specific route.
type RouteHandler struct {
	Path        string
	Method      string // GET, POST, etc. (empty matches all methods).
	HandlerFunc http.HandlerFunc
}

// MockHttpServerOpts configures the mock server.
type MockHttpServerOpts struct {
	DefaultStatusCode int            // Default response if no route matches.
	Routes            []RouteHandler // Route-specific handlers.
}

// MockProvisioningServerOpts is a struct that holds the options for the mock provisioning server.
type MockProvisioningServerOpts struct {
	ResponseCode       int
	ResponseType       string
	DownloadStatusCode int
	ProvisioningType   appConstants.ProvisioningType
	DownloadContent    []byte
}

// MockRegistrationServer creates a configurable HTTP test server with a customizable RegistrationResponse.
func MockRegistrationServer(t *testing.T, response dto.RegistrationResponse) *httptest.Server {
	t.Helper()

	ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(response)
	}))
	return ts
}

// MockProvisioningServer sets up a fake provisioning server which allows to
// customize what it returns via the mockProvisioningServerOpts struct.
func MockProvisioningServer(t *testing.T, opts MockProvisioningServerOpts) *httptest.Server {
	t.Helper()
	var server *httptest.Server

	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		case strings.Contains(r.URL.Path, "/find"):
			if opts.ResponseCode >= 400 {
				http.Error(w, "provisioning error", opts.ResponseCode)
				return
			}
			resp := dto.AgentProvisioningInfoResponse{
				Id:            "test-id",
				Type:          opts.ProvisioningType,
				DownloadUrl:   server.URL + "/download", // Ensure full URL
				Version:       "2.0.0",
				Sha256:        "",
				Size:          123456,
				DownloadToken: "test-token",
			}
			w.Header().Set("Content-Type", "application/json")
			_ = json.NewEncoder(w).Encode(resp)

		case strings.Contains(r.URL.Path, "/download"):
			if opts.DownloadStatusCode >= 400 {
				http.Error(w, "download error", opts.DownloadStatusCode)
				return
			}
			w.Header().Set("Content-Type", "application/octet-stream")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write(opts.DownloadContent)

		default:
			w.WriteHeader(http.StatusNotFound)
		}
	})

	server = httptest.NewServer(handler)
	return server
}

// GetFreePort finds a free port on the local machine.
func GetFreePort(t *testing.T) int {
	t.Helper()

	l, err := net.Listen("tcp", ":0")
	if err != nil {
		t.Fatalf("could not find a free port: %v", err)
	}
	defer l.Close()

	return l.Addr().(*net.TCPAddr).Port
}
