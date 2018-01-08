apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: heapster
  namespace: kube-system
  labels:
    k8s-addon: monitoring-standalone.addons.k8s.io
    k8s-app: heapster
    kubernetes.io/cluster-service: "true"
    version: v1.6.0
spec:
  replicas: 1
  selector:
    matchLabels:
      k8s-app: heapster
      version: v1.6.0
  template:
    metadata:
      labels:
        k8s-app: heapster
        version: v1.6.0
      annotations:
        scheduler.alpha.kubernetes.io/critical-pod: ""
        scheduler.alpha.kubernetes.io/tolerations: "[{\"key\":\"CriticalAddonsOnly\", \"operator\":\"Exists\"}]"
    spec:
      serviceAccountName: heapster
      containers:
        - image: ${heapster_image}
          name: heapster
          livenessProbe:
            httpGet:
              path: /healthz
              port: 8082
              scheme: HTTP
            initialDelaySeconds: 180
            timeoutSeconds: 5
          resources:
            # keep request = limit to keep this container in guaranteed class
            limits:
              cpu: 100m
              memory: 300Mi
            requests:
              cpu: 100m
              memory: 300Mi
          command:
            - /heapster
            - --source=kubernetes:https://kubernetes.default
            - --sink=influxdb:http://${influxdb_service_name}.kube-system.svc:8086
---
apiVersion: v1
kind: Service
metadata:
  name: heapster
  namespace: kube-system
  labels:
    k8s-addon: monitoring-standalone.addons.k8s.io
    kubernetes.io/name: "Heapster"
    kubernetes.io/cluster-service: "true"
spec:
  ports:
    - port: 80
      targetPort: 8082
  selector:
    k8s-app: heapster
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: heapster
  namespace: kube-system
  labels:
    k8s-addon: monitoring-standalone.addons.k8s.io
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: heapster
  labels:
    k8s-addon: monitoring-standalone.addons.k8s.io
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:heapster
subjects:
- kind: ServiceAccount
  name: heapster
  namespace: kube-system