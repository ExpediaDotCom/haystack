apiVersion: v1
kind: Service
metadata:
  name: elasticsearch-logging
  namespace: kube-system
  labels:
    k8s-app: elasticsearch-logging
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
    kubernetes.io/name: "Elasticsearch"
spec:
  ports:
  - port: 9200
    protocol: TCP
    targetPort: db
  selector:
    k8s-app: elasticsearch-logging
---

---
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: fluentd
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods", "namespaces"]
  verbs: ["get", "watch", "list"]
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: fluentd
  namespace: kube-system
---
# This role binding allows "system:serviceaccount:logs:default" to read pods in the "default" namespace.
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: fluentd
subjects:
- kind: ServiceAccount
  name: fluentd
  namespace: kube-system
roleRef:
  kind: ClusterRole
  name: fluentd
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
  labels:
    k8s-app: fluentd-logging
    kubernetes.io/cluster-service: "true"
  name: fluentd-elasticsearch
  namespace: kube-system
spec:
  selector:
    matchLabels:
      k8s-app: fluentd-logging
      kubernetes.io/cluster-service: "true"
  template:
    metadata:
      annotations:
        scheduler.alpha.kubernetes.io/critical-pod: ""
      creationTimestamp: null
      labels:
        k8s-app: fluentd-logging
        kubernetes.io/cluster-service: "true"
      name: fluentd-elasticsearch
      namespace: kube-system
    spec:
      serviceAccount: fluentd
      hostNetwork: true
      containers:
      - env:
        - name: AWS_ELASTICSEARCH_URL
          value: ${aws_elastic_search_url}
        image: ${fluentd_image}
        imagePullPolicy: IfNotPresent
        name: fluentd-elasticsearch
        resources:
          limits:
            memory: 500Mi
          requests:
            cpu: 100m
            memory: 200Mi
        volumeMounts:
        - mountPath: /var/log
          name: varlog
        - mountPath: /var/lib/docker/containers
          name: varlibdockercontainers
          readOnly: true
      restartPolicy: Always
      volumes:
      - hostPath:
          path: /var/log
        name: varlog
      - hostPath:
          path: /var/lib/docker/containers
        name: varlibdockercontainers
