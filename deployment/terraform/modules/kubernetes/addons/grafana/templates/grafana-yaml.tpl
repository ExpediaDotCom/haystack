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
        - mountPath: /etc/ssl/certs
          name: ssl-cert-volume
        env:
        - name: INFLUXDB_HOST
          value: monitoring-influxdb
        - name: GF_SERVER_HTTP_PORT
          value: "3000"
{{- if has (datasource "config") "smtp" }}
        - name: GF_SMTP_ENABLED
          value: "{{ (datasource "config").smtp.enabled }}"
        - name: GF_SMTP_FROM_ADDRESS
          value: "{{ (datasource "config").smtp.fromAddress }}"
        - name: GF_SMTP_FROM_NAME
          value: "{{ (datasource "config").smtp.fromName }}"
        - name: GF_SMTP_HOST
          value: "{{ (datasource "config").smtp.host }}"
        - name: GF_SMTP_USER
          value: "{{ (datasource "config").smtp.user }}"
        - name: GF_SMTP_PASSWORD
          value: "{{ (datasource "config").smtp.password }}"
{{- end }}
{{- if has (datasource "config") "rootUrl" }}
        - name: GF_SERVER_ROOT_URL
          value: "{{ (datasource "config").rootUrl }}"
{{- end }}
{{- if has (datasource "config") "nodeSelector" }}
      nodeSelector:
{{ (datasource "config").nodeSelector | toYAML | strings.Indent 8 }}
{{- end }}
      volumes:
      - name: ssl-cert-volume
        hostPath:
           path: /etc/ssl/certs
  volumeClaimTemplates:
   - metadata:
       name: grafana-persistent-storage
       annotations:
         volume.beta.kubernetes.io/storage-class: "{{ (datasource "config").storageClass }}"
     spec:
       storageClassName: "{{ (datasource "config").storageClass }}"
       accessModes: ["ReadWriteOnce"]
       resources:
         requests:
           storage: {{ (datasource "config").storage }}
---
apiVersion: v1
kind: Service
metadata:
  labels:
    kubernetes.io/cluster-service: 'true'
    kubernetes.io/name: monitoring-grafana
  name: monitoring-grafana
  namespace: kube-system
spec:
  ports:
  - port: 80
    targetPort: 3000
  selector:
    k8s-app: grafana
