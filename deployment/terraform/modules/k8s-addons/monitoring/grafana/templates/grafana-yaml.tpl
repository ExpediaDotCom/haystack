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
        image: gcr.io/google_containers/heapster-grafana-amd64:v4.4.3
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
        - mountPath: /etc/ssl/certs
          name: etc-ssl-certs
        env:
        - name: INFLUXDB_HOST
          value: monitoring-influxdb
        - name: GF_SERVER_HTTP_PORT
          value: "3000"
        - name: GF_SERVER_ROOT_URL
          value: "https://hcom-k8s.haystack.exp-int.net"
      nodeSelector:
        kops.k8s.io/instancegroup: "monitoring-nodes"
      volumes:
      - name: etc-ssl-certs
        hostPath:
          path: /etc/ssl/certs
  volumeClaimTemplates:
  - metadata:
      name: grafana-persistent-storage
      annotations:
        volume.beta.kubernetes.io/storage-class: "default"
    spec:
      storageClassName: "default"
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: "2Gi"