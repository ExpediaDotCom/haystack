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
            memory: ${memory_limit}Mi
        env:
        - name: "ES_JAVA_OPTS"
          value: "-Xms${jvm_memory_limit}m -Xmx${jvm_memory_limit}m"
        - name: "bootstrap.memory_lock"
          value: "true"
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