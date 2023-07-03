#!/usr/bin/env bash

mkdir -p build
cd fakespans
go get github.com/Shopify/sarama
go get github.com/codeskyblue/go-uuid
go get github.com/golang/protobuf/proto


for GOOS in darwin linux windows; do
    for GOARCH in 386 amd64; do
      echo "Building for ${GOOS} - ${GOARCH}"
      export GOOS=${GOOS}
      export GOARCH=${GOARCH}
      go build -v  -o ../build/fakespans-$GOOS-$GOARCH
    done
done