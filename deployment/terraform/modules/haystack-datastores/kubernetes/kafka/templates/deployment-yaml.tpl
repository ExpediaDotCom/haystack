kind: Deployment
apiVersion: apps/v1beta2
metadata:
  labels:
    k8s-app: ${app_name}
  name: ${app_name}
  namespace: ${namespace}
spec:
  replicas: ${replicas}
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
            memory: ${memory_limit}
        ports:
        - containerPort: ${container_port}
          hostPort: ${container_port}
        env:
        - name: "KAFKA_ADVERTISED_HOST_NAME"
          value: "${host_name}"
        - name: "KAFKA_ADVERTISED_PORT"
          value: "${service_port}"
        - name: "KAFKA_ZOOKEEPER_CONNECT"
          value: "${zk_endpoint}"
        - name: "KAFKA_CREATE_TOPICS"
          value: "${topics}"
        - name: "KAFKA_LOG_MESSAGE_TIMESTAMP_TYPE"
          value: "LogAppendTime"
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