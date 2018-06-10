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
        volumeMounts:
          # Create on-disk volume to store exec logs
        - mountPath: /config
          name: config-volume
        resources:
          limits:
            cpu: ${cpu_limit}
            memory: ${memory_limit}Mi
          requests:
            cpu: ${cpu_request}
            memory: ${memory_request}Mi
        livenessProbe:
          exec:
            command:
            - grep
            - "true"
            - /app/isHealthy
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 1
        env:
        - name: "HAYSTACK_OVERRIDES_CONFIG_PATH"
          value: "/config/trace-reader.conf"
        - name: "HAYSTACK_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "HAYSTACK_GRAPHITE_PORT"
          value: "${graphite_port}"
        - name: "HAYSTACK_GRAPHITE_ENABLED"
          value: "${graphite_enabled}"
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
      volumes:
      - name: config-volume
        configMap:
          name: ${configmap_name}

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
