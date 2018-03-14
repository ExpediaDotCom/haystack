apiVersion: v1
kind: ConfigMap
metadata:
 name: ${app_name}
 namespace: ${namespace}
data:
  haystack-metric-schemas.conf: |-

    [haystack_metrics]
    pattern = ^([a-z\-]+)\.([^.]+)\.haystack.*
    retentions = 1m:1d:1sec:1,5m:7d:1sec:1,15m:30d:1sec:1,1h:1y:1sec:1
    reorderBuffer = 20

    [default]
    pattern = .*
    retentions = 1m:1d:1sec:1,5m:7d:1sec:1,15m:30d:1sec:1,1h:1y:1sec:1
    reorderBuffer = 20
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