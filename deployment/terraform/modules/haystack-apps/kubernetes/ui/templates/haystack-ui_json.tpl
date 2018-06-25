{
  "port": 8080,
  "cluster": true,
  "upstreamTimeout": 30000,
  "enableServicePerformance": false,
  "enableServiceLevelTrends": false,
  "enableLatencyCostViewer": true,
  "graphite": {
    "host": "${graphite_hostname}",
    "port": ${graphite_port}
  },
  "connectors": {
    "traces": {
      "connectorName": "haystack",
      "haystackHost": "${trace_reader_hostname}",
      "haystackPort": ${trace_reader_service_port},
      "fieldKeys": [${whitelisted_fields}],
      "grpcOptions": {
        "grpc.max_receive_message_length": 52428800
      }
    },
    "trends": {
      "connectorName": "haystack",
      "metricTankUrl": "http://${metrictank_hostname}:${metrictank_port}",
      "encoder": "${metricpoint_encoder_type}"

    },
    "alerts": {
      "connectorName": "haystack",
      "metricTankUrl": "http://${metrictank_hostname}:${metrictank_port}",
      "alertFreqInSec": 300,
      "alertMergeBufferTimeInSec": 60,
      "subscriptions": {
          "connectorName": "stub",
          "enabled": false
      }
    }
  },
  "enableSSO": ${ui_enable_sso},
  "saml": {
    "callbackUrl": "${ui_saml_callback_url}",
    "entry_point": "${ui_saml_entry_point}",
    "issuer": "${ui_saml_issuer}"
  },
  "sessionTimeout": 3600000,
  "sessionSecret": "${ui_session_secret}"
}
