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
            memory: ${memory_limit}Mi
        env:
        - name: "MAX_HEAP_SIZE"
          value: "${memory_limit}m"
        - name: "HEAP_NEWSIZE"
          value: "${memory_limit}m"
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