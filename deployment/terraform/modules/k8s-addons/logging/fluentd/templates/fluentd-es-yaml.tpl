apiVersion: v1
kind: ConfigMap
data:
  containers.input.conf: |-
    <source>
      type tail
      path /var/log/containers/*.log
      pos_file /var/log/es-containers.log.pos
      time_format %Y-%m-%dT%H:%M:%S.%NZ
      tag kubernetes.*
      read_from_head true
      format multi_format
      <pattern>
        format json
        time_key time
        time_format %Y-%m-%dT%H:%M:%S.%NZ
      </pattern>
      <pattern>
        format /^(?<time>.+) (?<stream>stdout|stderr) [^ ]* (?<log>.*)$/
        time_format %Y-%m-%dT%H:%M:%S.%N%:z
      </pattern>
    </source>
  output.conf: |-
    # Enriches records with Kubernetes metadata
    <filter kubernetes.**>
      type kubernetes_metadata
    </filter>
    <match **>
       type elasticsearch
       log_level info
       include_tag_key true
       host ${elasticsearch_host}
       port ${elasticsearch_port}
       logstash_format true
       # Set the chunk limits.
       buffer_chunk_limit 2M
       buffer_queue_limit 8
       flush_interval 5s
       # Never wait longer than 5 minutes between retries.
       max_retry_wait 30
       # Disable the limit on the number of retries (retry forever).
       disable_retry_limit
       # Use multiple threads for processing.
       num_threads 2
    </match>
metadata:
  name: fluentd-es-config
  namespace: kube-system
  labels:
    addonmanager.kubernetes.io/mode: Reconcile
---
apiVersion: v1
kind: Service
metadata:
  name: fluentd
  namespace: kube-system
  labels:
    k8s-app: fluentd
    kubernetes.io/cluster-service: "true"
    addonmanager.kubernetes.io/mode: Reconcile
    kubernetes.io/name: "Elasticsearch"
spec:
  ports:
  - port: 9200
    protocol: TCP
    targetPort: db
  selector:
    k8s-app: fluentd
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
      hostNetwork: false
      containers:
      - env:
        - name: FLUENTD_ARGS
          value: --no-supervisor -q
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
        - mountPath: ${container_log_path}
          name: varlibdockercontainers
          readOnly: true
        - name: config-volume
          mountPath: /etc/fluent/config.d
      restartPolicy: Always
      volumes:
      - hostPath:
          path: /var/log
        name: varlog
      - hostPath:
          path: ${container_log_path}
        name: varlibdockercontainers
      - name: config-volume
        configMap:
          name: fluentd-es-config
