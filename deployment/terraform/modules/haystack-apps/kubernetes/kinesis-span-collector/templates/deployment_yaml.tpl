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
        ${env_vars}  
      nodeSelector:
        ${node_selecter_label}
      volumes:
      - name: config-volume
        configMap:
          name: ${configmap_name}

