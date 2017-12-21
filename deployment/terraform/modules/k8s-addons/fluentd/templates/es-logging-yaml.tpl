# RBAC authn and authz
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${elasticsearch-name}
  namespace: kube-system
  labels:
    k8s-app: ${elasticsearch-name}
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
---
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: ${elasticsearch-name}
  labels:
    k8s-app: ${elasticsearch-name}
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
rules:
- apiGroups:
  - ""
  resources:
  - "services"
  - "namespaces"
  - "endpoints"
  verbs:
  - "get"
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  namespace: kube-system
  name: ${elasticsearch-name}
  labels:
    k8s-app: ${elasticsearch-name}
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
subjects:
- kind: ServiceAccount
  name: ${elasticsearch-name}
  namespace: kube-system
  apiGroup: ""
roleRef:
  kind: ClusterRole
  name: ${elasticsearch-name}
  apiGroup: ""
---
# Elasticsearch deployment itself
apiVersion: apps/v1beta1
kind: StatefulSet
metadata:
  name: ${elasticsearch-name}
  namespace: kube-system
  labels:
    k8s-app: ${elasticsearch-name}
    version: v5.6.4
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
spec:
  serviceName: ${elasticsearch-name}
  replicas: 1
  selector:
    matchLabels:
      k8s-app: ${elasticsearch-name}
      version: v5.6.4
  template:
    metadata:
      labels:
        k8s-app: ${elasticsearch-name}
        version: v5.6.4
        kubernetes.io/cluster-service: "true"
    spec:
      serviceAccountName: ${elasticsearch-name}
      containers:
      - image: k8s.gcr.io/elasticsearch:v5.6.5
        name: ${elasticsearch-name}
        resources:
          # need more cpu upon initialization, therefore burstable class
          limits:
            cpu: 1000m
          requests:
            cpu: 100m
        ports:
        - containerPort: 9200
          name: db
          protocol: TCP
        - containerPort: 9300
          name: transport
          protocol: TCP
        volumeMounts:
        - name: ${elasticsearch-name}
          mountPath: /data
        env:
        - name: "NAMESPACE"
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
      volumes:
      - name: ${elasticsearch-name}
        emptyDir: {}
      # Elasticsearch requires vm.max_map_count to be at least 262144.
      # If your OS already sets up this number to a higher value, feel free
      # to remove this init container.
      initContainers:
      - image: alpine:3.6
        command: ["/sbin/sysctl", "-w", "vm.max_map_count=262144"]
        name: ${elasticsearch-name}-init
        securityContext:
          privileged: true
---
apiVersion: v1
kind: Service
metadata:
  name: ${elasticsearch-name}
  namespace: kube-system
  labels:
    k8s-app: ${elasticsearch-name}
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
    kubernetes.io/name: "Elasticsearch"
spec:
  ports:
  - port: 9200
    protocol: TCP
    targetPort: db
  selector:
    k8s-app: ${elasticsearch-name}
---
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: kibana-logging
  namespace: kube-system
  labels:
    k8s-app: kibana-logging
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
spec:
  replicas: 1
  selector:
    matchLabels:
      k8s-app: kibana-logging
  template:
    metadata:
      labels:
        k8s-app: kibana-logging
    spec:
      containers:
      - name: kibana-logging
        image: docker.elastic.co/kibana/kibana:5.6.5
        resources:
          # need more cpu upon initialization, therefore burstable class
          limits:
            cpu: 1000m
          requests:
            cpu: 100m
        env:
          - name: ELASTICSEARCH_URL
            value: http://${elasticsearch-name}:9200
          - name: SERVER_BASEPATH
            value: /kibana
          - name: XPACK_MONITORING_ENABLED
            value: "false"
          - name: XPACK_SECURITY_ENABLED
            value: "false"
        ports:
        - containerPort: 5601
          name: ui
          protocol: TCP
---
apiVersion: v1
kind: Service
metadata:
  name: kibana-logging
  namespace: kube-system
  labels:
    k8s-app: kibana-logging
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
    kubernetes.io/name: "Kibana"
spec:
  ports:
  - port: 5601
    protocol: TCP
    targetPort: ui
  selector:
    k8s-app: kibana-logging