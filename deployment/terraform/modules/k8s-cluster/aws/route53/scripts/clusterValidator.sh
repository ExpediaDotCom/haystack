#!/usr/bin/env bash

kubectl="$1"
context="$2"

shouldExit="false"
while [[ "$shouldExit" == "false" ]]
   do
    nodes=$(${kubectl} get nodes --context ${context} -l kubernetes.io/role=master -o json  | jq -r .items)
    shouldExit="true"

    for row in $(echo "${nodes}" | jq -r '.[] | @base64'); do
    _jq() {
     echo ${row} | base64 --decode | jq -r ${1}
    }

   conditions=$(echo $(_jq '.status.conditions'))

   readyNodes=$(echo ${conditions} | jq -e '.[] | select(.type=="Ready" and .status=="True")')
   exit_status=$(echo $?)

   echo $readyNodes
   if [[ $exit_status -gt 0 ]]; then
    echo "false"
    shouldExit="false"
    break
   fi

   done
   sleep 5
done


