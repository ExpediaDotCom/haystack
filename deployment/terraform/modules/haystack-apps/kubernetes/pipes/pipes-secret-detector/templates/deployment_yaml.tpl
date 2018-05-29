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
        - name: "HAYSTACK_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "HAYSTACK_GRAPHITE_PORT"
          value: "${graphite_port}"
        - name: "HAYSTACK_KAFKA_BROKERS"
          value: "${kafka_hostname}"
        - name: "HAYSTACK_KAFKA_THREADCOUNT"
          value: "${pipes_secret_detector_kafka_threadcount}"
        - name: "HAYSTACK_SECRETSNOTIFICATIONS_EMAIL_FROM"
          value: "${pipes_secret_detector_secretsnotifications_email_from}"
        - name: "HAYSTACK_SECRETSNOTIFICATIONS_EMAIL_HOST"
          value: "${pipes_secret_detector_secretsnotifications_email_host}"
        - name: "HAYSTACK_SECRETSNOTIFICATIONS_EMAIL_SUBJECT"
          value: "${pipes_secret_detector_secretsnotifications_email_subject}"
        - name: "HAYSTACK_SECRETSNOTIFICATIONS_EMAIL_TOS"
          value: "${pipes_secret_detector_secretsnotifications_email_tos}"
        - name: "HAYSTACK_SECRETSNOTIFICATIONS_WHITELIST_BUCKET"
          value: "${pipes_secret_detector_secretsnotifications_whitelist_bucket}"
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

