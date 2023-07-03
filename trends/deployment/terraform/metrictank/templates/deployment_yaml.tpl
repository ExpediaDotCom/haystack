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
            cpu: ${cpu_limit}
            memory: ${memory_limit}Mi
          requests:
            cpu: ${cpu_request}
            memory: ${memory_request}Mi
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
        - name: "MT_STATS_ADDR"
          value: "${graphite_address}"
        - name: "MT_MEMORY_IDX_TAG_SUPPORT"
          value: "${tag_support}"
        ${env_vars}
      nodeSelector:
        ${node_selecter_label}

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
