#!/usr/bin/env bash

kubectl="$1"
context="$2"

shouldExit="false"
while [[ "$shouldExit" == "false" ]]
do
    sleep 15
    nodes=$(${kubectl} get nodes --context ${context} -l kubernetes.io/role=master -o json  | jq -r .items)
    getNodes_exit_status=$(echo $?)

    if [[ ${#nodes} -lt 3 || $getNodes_exit_status -gt 0 ]]; then
        echo "Retrying for getting nodes info"
        continue
    fi

    shouldExit="true"

    for row in $(echo "${nodes}" | jq -r '.[] | @base64'); do
        _jq() {
         echo ${row} | base64 --decode | jq -r ${1}
        }

       conditions=$(echo $(_jq '.status.conditions'))

       readyNodes=$(echo ${conditions} | jq -e '.[] | select(.type=="Ready" and .status=="True")')
       readyNodes_exit_status=$(echo $?)
       echo $readyNodes_exit_status

       echo "ready Nodes"
       echo $readyNodes

       if [[ $readyNodes_exit_status -gt 0 ]]; then
           echo "false"
           echo "Retrying for getting ready nodes"
           shouldExit="false"
           break
       fi
    done
done


