#!/usr/bin/env bash

datasource_location="datasources"
dashboard_location="dashboards"

if [ -z ${grafana_user} ]; then
    grafana_user="admin"
fi

if [ -z ${grafana_passwd} ]; then
    grafana_passwd="admin"
fi

if [ -z ${gf_server_protocol} ]; then
    gf_server_protocol="admin"
fi

if [ -z ${gf_server_hostname} ]; then
    gf_server_hostname="monitoring-grafana"
fi

if [ -z ${httpPort} ]; then
    httpPort="2003"
fi

echo "gf_server_hostname=${gf_server_hostname}"

echo "Going to create haystack datasources"
DATASOURCE_GRAFANA_URL="${gf_server_protocol}://${grafana_user}:${grafana_passwd}@${gf_server_hostname}:${httpPort}/api/datasources"
for FILENAME in ${datasource_location}/*.json; do
    echo "Creating datasource ${FILENAME}"
    curl -X POST -H "Content-Type:application/json" "${DATASOURCE_GRAFANA_URL}" -d @${FILENAME}
    echo ""
done

echo "Going to create haystack dashboards"
DASHBOARD_GRAFANA_URL="${gf_server_protocol}://${grafana_user}:${grafana_passwd}@${gf_server_hostname}:${httpPort}/api/dashboards/db"
for FILENAME in ${dashboard_location}/*.json; do
    echo "Creating dashboard ${FILENAME}"
    curl -X POST -H "Content-Type:application/json" "${DASHBOARD_GRAFANA_URL}" -d @${FILENAME}
    echo ""
done