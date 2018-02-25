{
  "port": 8080,
  "cluster": true,
  "upstreamTimeout": 30000,
  "enableServicePerformance": true,
  "enableServiceLevelTrends": true,
  "enableLatencyCostViewer": false,
  "graphite": {
    "host": "${graphite_hostname}",
    "port": ${graphite_port}
  },
  "connectors": {
    "traces": {
      "connectorName": "haystack",
      "haystackHost": "${trace_reader_hostname}",
      "haystackPort": ${trace_reader_service_port}
    },
    "trends": {
      "connectorName": "haystack",
      "metricTankUrl": "http://${metrictank_hostname}:${metrictank_port}"
    }
  }
}