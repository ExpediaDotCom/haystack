#!/usr/bin/env bash

usage() {
    cat <<EOF
DESCRIPTION:
    Utility script to haystack-ui with a zipkin instance as backend for traces. 
    It also spins sleuth-webmvc-example services for feeding data in Zipkin cluster and sets up some traces in Zipkin for haystack-ui to display.

PREREQUISITES:
    Assumes that you have mvn and git available on your machine. 
    haystack-ui must be already installed (npm install) and built (npm build), if not please install and build before running this script

USAGE: 
    ./zipkin-quickstart

OPTIONS:
    -h help
    -d debug mode, will emit out all logs from zipkin and sleuth-webmvc-example
EOF
}

fetchSleuthExample() {
    printf '\n=> downloading sleuth webmvc example\n\n'
    git clone https://github.com/openzipkin/sleuth-webmvc-example.git
}

runSleuthExample() {
    printf '\n=> %s\n\n' "running sleuth webmvc ${1}"
    if [[ "$2" = '-d' ]]; then
        mvn compile exec:java -Dexec.mainClass=sleuth.webmvc.${1}  &
    else
        mvn compile exec:java -Dexec.mainClass=sleuth.webmvc.${1}  > /dev/null 2>&1 &
    fi
}

downloadAndRunZipkin() {
    printf '\n=> downloading and running zipkin\n\n'
    curl -sSL https://zipkin.io/quickstart.sh | bash -s

    if [[ "$1" = '-d' ]]; then
        java -jar zipkin.jar  &
    else
        java -jar zipkin.jar  > /dev/null 2>&1 &
    fi
}

downloadAndRunZipkin() {
    printf '\n=> downloading and running zipkin\n\n'
    curl -sSL https://zipkin.io/quickstart.sh | bash -s
    java -jar zipkin.jar  > /dev/null 2>&1 &
}

feedTraces() {
    printf '\n=> waiting for zipkin to start and pushing sample traces\n\n'
    sleep 60
    curl http://localhost:8081
    curl http://localhost:8081
    curl http://localhost:8081
    curl http://localhost:8081
    curl http://localhost:8081
    curl http://localhost:8081
    curl http://localhost:9000/api
    curl http://localhost:9000/api
    curl http://localhost:9000/api
    curl http://localhost:9000/api
    sleep 60
}

startHaystackUi() {
    printf '\n=> starting haystack-ui\n\n'
    cd ../../../
    HAYSTACK_OVERRIDES_CONFIG_PATH=../../build/zipkin/base.json npm start
}

main() {
    if [[ "$1" = '-h' || "$1" = '--help' ]]; then
        usage
        exit
    fi

    # execution directory 
    local WORKSPACE=./zipkin-workspace
    rm -rf $WORKSPACE
    mkdir $WORKSPACE
    cd $WORKSPACE

    # download and run zipkin
    downloadAndRunZipkin "$1"
    ZIPKIN_PROC_ID=$!
    printf '\n=> zipkin proc id %s\n\n' "${ZIPKIN_PROC_ID}"

    # download and run sleuth example backend and frontend
    fetchSleuthExample
    cd sleuth-webmvc-example
    runSleuthExample "Backend" "$1"
    SLEUTH_BACKEND_PROC_ID=$!
    printf '\n=> backend proc id %s\n\n' "${SLEUTH_BACKEND_PROC_ID}"
    runSleuthExample "Frontend" "$1"
    SLEUTH_FRONTEND_PROC_ID=$!
    printf '\n=> frontend proc id %s\n\n' "${SLEUTH_FRONTEND_PROC_ID}"
    cd ..
    
    # feed traces to zipkin
    feedTraces

    # run haystack ui
    startHaystackUi

    # teardown services 
    printf '\n=> tearing down\n\n'
    kill $SLEUTH_BACKEND_PROC_ID
    kill $SLEUTH_FRONTEND_PROC_ID
    kill $ZIPKIN_PROC_ID
}

main "$@"
