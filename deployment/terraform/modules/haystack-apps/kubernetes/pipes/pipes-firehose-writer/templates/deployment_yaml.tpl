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
            memory: ${memory_limit}Mi
          requests:
            cpu: ${cpu_limit}
            memory: ${memory_limit}Mi
        env:
        - name: "HAYSTACK_FIREHOSE_INITIALRETRYSLEEP"
          value: "${firehose_initialretrysleep}"
        - name: "HAYSTACK_FIREHOSE_MAXRETRYSLEEP"
          value: "${firehose_maxretrysleep}"
        - name: "HAYSTACK_FIREHOSE_SIGNINGREGION"
          value: "${firehose_region}"
        - name: "HAYSTACK_FIREHOSE_STREAMNAME"
          value: "${firehose_stream_name}"
        - name: "HAYSTACK_FIREHOSE_URL"
          value: "${firehose_url}"
        - name: "HAYSTACK_KAFKA_FROMTOPIC"
          value: "${firehose_writer_haystack_kafka_fromtopic}"
        - name: "HAYSTACK_KAFKA_THREADCOUNT"
          value: "${firehose_kafka_threadcount}"
        - name: "HAYSTACK_KAFKA_BROKERS"
          value: "${kafka_hostname}"
        - name: "HAYSTACK_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "HAYSTACK_GRAPHITE_PORT"
          value: "${graphite_port}"
        - name: "HAYSTACK_FIREHOSE_USESTRINGBUFFERING"
          value: "${firehose_usestringbuffering}"
        - name: "HAYSTACK_FIREHOSE_MAXBATCHINTERVAL"
          value: "${firehose_maxbatchinterval}"
        - name: "JAVA_XMS"
          value: "${jvm_memory_limit}m"
        - name: "JAVA_XMX"
          value: "${jvm_memory_limit}m"
        ${env_vars}
        livenessProbe:
          exec:
            command:
            - grep
            - "true"
            - /app/isHealthy
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 6
      nodeSelector:
        ${node_selecter_label}

