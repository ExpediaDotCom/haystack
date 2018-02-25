{
  "port": 8080,
  "cluster": true,
  "upstreamTimeout": 60000,
  "stores": {
    "traces": {
      "storeName": "haystack",
      "haystackHost": "${trace_reader_hostname}",
      "haystackPort": ${trace_reader_service_port}
    },
    "trends": {
      "storeName": "haystack",
      "metricTankUrl": "http://${metrictank_hostname}:${metrictank_port}"
    }
  }
}