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
        - name: "HAYSTACK_FIREHOSE_SIGNINGREGION"
          value: "${firehose_region}"
        - name: "HAYSTACK_FIREHOSE_STREAMNAME"
          value: "${firehose_stream_name}"
        - name: "HAYSTACK_FIREHOSE_URL"
          value: "${firehose_url}"
        - name: "HAYSTACK_KAFKA_BROKERS"
          value: "${kafka_hostname}"
        - name: "HAYSTACK_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "HAYSTACK_GRAPHITE_PORT"
          value: "${graphite_port}"
        ${env_vars}
      nodeSelector:
        ${node_selecter_label}

