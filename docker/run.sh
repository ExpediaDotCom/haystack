#!/usr/bin/env bash

curl -XPOST -H "Content-Type: application/json" -d '{
        "fields": [{
            "name": "error",
            "type": "string",
            "enabled": true
        }, {
            "name": "http-status",
            "type": "string",
            "aliases": [ "http.status_code" ],
            "enabled": true
        }]
}' "http://localhost:9200/reload-configs/whitelist-index-fields/1" 2>1 1>/dev/null

COUNT=0
URL=$1

[[ -z ${URL} ]] && URL=http://localhost:9090/hello

while true
do
    COUNT=$((COUNT+1))
    curl ${URL}
    echo " ${COUNT}"
    sleep 1
done
