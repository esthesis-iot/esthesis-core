#!/usr/bin/env sh

build() {
  GOOS=$1
  GOARCH=$2
  EXT=$3
  echo "Building $GOOS/$GOARCH"
  cd ../cmd && GOOS=$GOOS GOARCH=$GOARCH go build -o ../releases/$VERSION/esthesis-device-$GOOS-$GOARCH$EXT && cd ../scripts
}

# Match version
VERSION=$(sed -nr 's/const Version = "(.*)"/\1/p' ../internal/pkg/config/config.go)
echo "Building version $VERSION"

#build darwin amd64
#build darwin arm64
#build freebsd amd64
#build freebsd arm
#build freebsd arm64
#build linux 386
build linux amd64
#build linux arm
#build linux arm64
#build openbsd 386
#build openbsd amd64
#build openbsd arm
#build openbsd arm64
#build windows 386 .exe
#build windows amd64 .exe
#build windows arm .exe
#build windows arm64 .exe

scp ../releases/"$VERSION"/esthesis-device-linux-amd64 nassos@esthesis-device-1.home.nassosmichas.com:/home/nassos
scp ../releases/"$VERSION"/esthesis-device-linux-amd64 nassos@esthesis-device-2.home.nassosmichas.com:/home/nassos
scp ../releases/"$VERSION"/esthesis-device-linux-amd64 nassos@esthesis-device-3.home.nassosmichas.com:/home/nassos
