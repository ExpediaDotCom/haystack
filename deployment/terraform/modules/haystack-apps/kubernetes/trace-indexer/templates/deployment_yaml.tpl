apiVersion: v1
kind: ConfigMap
metadata:
  name: ${app_name}
  namespace: ${namespace}
data:
  kinesis-span-collector.conf: "${config}"

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
        volumeMounts:
          # Create on-disk volume to store exec logs
        - mountPath: /config
          name: config-volume
        resources:
          limits:
            memory: ${memory_limit}
          requests:
            cpu: ${cpu_limit}
            memory: ${memory_limit}
        env:
        - name: "HAYSTACK_OVERRIDES_CONFIG_PATH"
          value: "/config/kinesis-span-collector.conf"
        - name: "HAYSTACK_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "HAYSTACK_GRAPHITE_PORT"
          value: "${graphite_port}"
        livenessProbe:
          httpGet:
            path: /
            port: 9090
          initialDelaySeconds: 30
          timeoutSeconds: 30
      nodeSelector:
        ${node_selecter_label}
      volumes:
      - name: config-volume
        configMap:
          name: ${app_name}

