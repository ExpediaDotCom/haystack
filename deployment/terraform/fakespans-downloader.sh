#!/usr/bin/env bash

get_latest_release() {
    KERNEL=`uname -s`
    MACHINE_TYPE=`uname -m`
    OS=""
    ARCH=""
    if      [ $KERNEL = "Darwin" ]; then
            OS="darwin"
    elif        [ $KERNEL = "Linux" ]; then
            OS="linux"
    elif        [ $KERNEL = "FreeBSD" ]; then
            OS="linux"
    else
            echo "Unsupported OS"
            exit 1;
    fi

    if [ ${MACHINE_TYPE} == 'x86_64' ]; then
      ARCH="amd64"
    else
      ARCH="386"
    fi

    echo "Downloading fakespans for ${OS} and architecture ${ARCH}"
    curl -L https://github.com/ExpediaDotCom/haystack-idl/releases/download/$(curl --silent "https://api.github.com/repos/ExpediaDotCom/haystack-idl/releases/latest" | jq -r .tag_name)/fakespans-${OS}-${ARCH} -o fakespans
    chmod a+x fakespans
}

run_fakespans() {
    ./fakespans --kafka-broker $(minikube ip):9092
}


if [ ! -f "fakespans" ]; then
    echo "fakespans not found. Downloading"
    get_latest_release
fi

echo "Running fakespans"
run_fakespans