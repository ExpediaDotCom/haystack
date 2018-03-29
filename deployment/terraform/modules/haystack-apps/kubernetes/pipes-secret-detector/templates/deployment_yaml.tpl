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
        - name: "HAYSTACK_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "HAYSTACK_GRAPHITE_PORT"
          value: "${graphite_port}"
        - name: "HAYSTACK_KAFKA_BROKERS"
          value: "${kafka_hostname}"
        - name: "HAYSTACK_SECRETSNOTIFICATION_EMAIL_FROM"
          value: "${pipes_secret_detector_secretsnotifications_email_from}"
        - name: "HAYSTACK_SECRETSNOTIFICATION_EMAIL_HOST"
          value: "${pipes_secret_detector_secretsnotifications_email_host}"
        - name: "HAYSTACK_SECRETSNOTIFICATION_EMAIL_SUBJECT"
          value: "${pipes_secret_detector_secretsnotifications_email_subject}"
        - name: "HAYSTACK_SECRETSNOTIFICATION_EMAIL_TOS"
          value: "${pipes_secret_detector_secretsnotifications_email_tos}"
        - name: "HAYSTACK_SECRETSNOTIFICATION_IGNORES_IPS_SERVICENAMES"
          value: "${pipes_secret_detector_secretsnotifications_ignores_ips_servicenames}"
        ${env_vars}
        livenessProbe:
          exec:
            command:
            - grep
            - "true"
            - /app/isHealthy
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 1
      nodeSelector:
        ${node_selecter_label}

