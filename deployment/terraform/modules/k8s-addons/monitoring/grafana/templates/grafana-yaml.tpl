apiVersion: apps/v1beta1
kind: StatefulSet
metadata:
  name: monitoring-grafana
  namespace: kube-system
spec:
  replicas: 1
  serviceName: monitoring-grafana
  template:
    metadata:
      labels:
        task: monitoring
        k8s-app: grafana
    spec:
      containers:
      - name: grafana
        image: ${grafana_image}
        resources:
          limits:
            cpu: 100m
            memory: 100Mi
          requests:
            cpu: 100m
            memory: 100Mi
        ports:
        - containerPort: 3000
          protocol: TCP
        volumeMounts:
        - mountPath: /var
          name: grafana-persistent-storage
        env:
        - name: INFLUXDB_HOST
          value: monitoring-influxdb
        - name: GF_SERVER_HTTP_PORT
          value: "3000"
        - name: GF_SERVER_ROOT_URL
          value: "${grafana_root_path}"
  volumeClaimTemplates:
   - metadata:
       name: grafana-persistent-storage
       annotations:
         volume.beta.kubernetes.io/storage-class: "${grafana_storage_class}"
     spec:
       storageClassName: "${grafana_storage_class}"
       accessModes: ["ReadWriteOnce"]
       resources:
         requests:
           storage: "${grafana_storage}"
---
apiVersion: v1
kind: Service
metadata:
  labels:
    kubernetes.io/cluster-service: "true"
    kubernetes.io/name: monitoring-grafana
  name: monitoring-grafana
  namespace: kube-system
spec:
  ports:
  - port: 80
    targetPort: 3000
  selector:
    k8s-app: grafana
