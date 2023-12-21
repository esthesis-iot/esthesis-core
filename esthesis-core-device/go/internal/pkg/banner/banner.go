package banner

import (
	"fmt"
	"os"
	"time"
)

var (
	Commit    string
	BuildTime string
)

func Print(version string) {
	fmt.Println(`***************************************************
https://esthes.is              esthesis@eurodyn.com

           _   _               _       _       _
  ___  ___| |_| |__   ___  ___(_)___  (_) ___ | |_
 / _ \/ __| __| '_ \ / _ \/ __| / __| | |/ _ \| __|
|  __/\__ \ |_| | | |  __/\__ \ \__ \ | | (_) | |_
 \___||___/\__|_| |_|\___||___/_|___/ |_|\___/ \__|`)
	fmt.Println("")
	fmt.Println("esthesis-core-device")
	_, _ = fmt.Fprintf(os.Stdout, "Version    : %s\n", version)
	_, _ = fmt.Fprintf(os.Stdout, "Commit     : %s\n", Commit)
	_, _ = fmt.Fprintf(os.Stdout, "Build time : %s\n", BuildTime)
	_, _ = fmt.Fprintf(os.Stdout, "Local time : %s\n", time.Now().Format(time.RFC3339))
	fmt.Println("***************************************************")
	fmt.Println()
}
