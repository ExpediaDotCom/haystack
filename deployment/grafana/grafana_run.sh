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
    gf_server_protocol="http"
fi

if [ -z ${gf_server_hostname} ]; then
    gf_server_hostname="monitoring-grafana"
fi

if [ -z ${gf_server_http_port} ]; then
    gf_server_http_port="3000"
fi

echo "gf_server_protocol=${gf_server_protocol}"
echo "gf_server_hostname=${gf_server_hostname}"
echo "gf_server_http_port=${gf_server_http_port}"

echo "Creating haystack datasources"
DATASOURCE_GRAFANA_URL="${gf_server_protocol}://${grafana_user}:${grafana_passwd}@${gf_server_hostname}:${gf_server_http_port}/api/datasources"
for FILENAME in ${datasource_location}/*.json; do
    echo "Creating datasource ${FILENAME}"
    curl -X POST -H "Content-Type:application/json" "${DATASOURCE_GRAFANA_URL}" -d @${FILENAME}
    echo ""
done

echo "Creating haystack dashboards"
DASHBOARD_GRAFANA_URL="${gf_server_protocol}://${grafana_user}:${grafana_passwd}@${gf_server_hostname}:${gf_server_http_port}/api/dashboards/db"
for FILENAME in ${dashboard_location}/*.json; do
    echo "Creating dashboard ${FILENAME}"
    curl -X POST -H "Content-Type:application/json" "${DASHBOARD_GRAFANA_URL}" -d @${FILENAME}
    echo ""
done