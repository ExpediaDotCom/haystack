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
        imagePullPolicy: ${image_pull_policy}
        volumeMounts:
        - name: config-volume
          mountPath: /config
        - name: aws
          mountPath: /root/.aws
        resources:
          limits:
            cpu: ${cpu_limit}
            memory: ${memory_limit}Mi
          requests:
            cpu: ${cpu_request}
            memory: ${memory_request}Mi
        env:
        - name: "AA_OVERRIDES_CONFIG_PATH"
          value: "/config/${app_name}.conf"
        - name: "AA_GRAPHITE_HOST"
          value: "${graphite_host}"
        - name: "AA_GRAPHITE_PORT"
          value: "${graphite_port}"
        - name: "AA_GRAPHITE_ENABLED"
          value: "${graphite_enabled}"
        - name: "JAVA_XMS"
          value: "${jvm_memory_limit}m"
        - name: "JAVA_XMX"
          value: "${jvm_memory_limit}m"
        ${env_vars}
        livenessProbe:
          httpGet:
            path: /alive
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 5
          failureThreshold: 6
      nodeSelector:
        ${node_selector_label}
      volumes:
      - name: config-volume
        configMap:
          name: ${configmap_name}
      - name: aws
        hostPath:
          path: /root/.aws
---
# ------------------- Service ------------------- #
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
