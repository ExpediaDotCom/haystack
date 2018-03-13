apiVersion: v1
kind: ConfigMap
metadata:
 name: haystack-ui-trends-test
 namespace: haystack-apps
data:
  haystack-ui.json: |-
    {
      "port": 8080,
      "cluster": true,
      "upstreamTimeout": 30000,
      "enableServicePerformance": false,
      "enableServiceLevelTrends": true,
      "enableLatencyCostViewer": false,
      "graphite": {
        "host": "monitoring-influxdb-graphite.kube-system.svc",
        "port": 2003
      },
      "connectors": {
        "traces": {
          "connectorName": "haystack",
          "haystackHost": "trace-reader",
          "haystackPort": 8080,
          "fieldKeys": [
            "traceId",
            "error",
            "minDuration",
            "tpid",
            "tuid",
            "du",
            "Client-Id",
            "msterrorlist",
            "companycode",
            "JurisdictionCode",
            "managementunitcode",
            "userguid"
          ],
          "grpcOptions": {
            "grpc.max_receive_message_length": 52428800
          }
        },
        "trends": {
          "connectorName": "haystack",
          "metricTankUrl": "http://metrictank.stockyard.us-west-2.prod.monitoring.expedia.com"
        }
      }
    }
---
# ------------------- Deployment ------------------- #

kind: Deployment
apiVersion: apps/v1beta2
metadata:
  labels:
    k8s-app: ${app_name}
  name: ${app_name}
  namespace: ${namespace}
spec:
  replicas: ${replicas}
  revisionHistoryLimit: 10
  selector:
    matchLabels:
      k8s-app: ${app_name}
  template:
    metadata:
      labels:
        k8s-app: ${app_name}
    spec:
      containers:
      - name: ${app_name}
        image: ${image}
        resources:
          limits:
            memory: ${memory_limit}
          requests:
            cpu: ${cpu_limit}
            memory: ${memory_limit}
        env:
        - name: "MT_HTTP_MULTI_TENANT"
          value: "false"
        - name: "MT_CARBON_IN_ENABLED"
          value: "false"
        - name: "MT_KAFKA_MDM_IN_ENABLED"
          value: "true"
        - name: "MT_CASSANDRA_ADDRS"
          value: "${cassandra_address}"
        - name: "MT_KAFKA_MDM_IN_BROKERS"
          value: "${kafka_address}"
        - name: "MT_CASSANDRA_IDX_HOSTS"
          value: "${cassandra_address}"
        - name: "MT_CASSANDRA_IDX_TIMEOUT"
          value: "100s"
        - name: "MT_CASSANDRA_WRITE_QUEUE_SIZE"
          value: "50"
        - name: "MT_STATS_ADDR"
          value: "${graphite_address}"
        - name: "MT_RETENTION_SCHEMAS_FILE"
          value: "/etc/metrictank/storage-config/haystack-metric-schemas.conf"
        - name: "MT_CHUNK_CACHE_MAX_SIZE"
          value: "536870912"
        ${env_vars}
        volumeMounts:
          # Create on-disk volume to store exec logs
        - mountPath: /etc/metrictank/storage-config
          name: storage-config-volume
      nodeSelector:
        ${node_selecter_label}
      volumes:
      - name: storage-config-volume
        configMap:
          name: ${app_name}


# ------------------- Service ------------------- #
---
apiVersion: v1
kind: Service
metadata:
  labels:
    k8s-app: ${app_name}
  name: ${app_name}
  namespace: ${namespace}
spec:
  ports:
  - port: ${service_port}
    targetPort: ${container_port}
  selector:
    k8s-app: ${app_name}