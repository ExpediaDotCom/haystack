apiVersion: v1
kind: ConfigMap
metadata:
 name: influxdb-configmap
 namespace: kube-system
data:
  influxdb.toml: |-
    reporting-disabled = true
    bind-address = "localhost:8088"

    [admin]
      enabled = true
    [meta]
      dir = "/data/meta"
      retention-autocreate = true
      logging-enabled = true

    [data]
      dir = "/data/data"
      wal-dir = "/data/wal"
      query-log-enabled = true
      cache-max-memory-size = 1073741824
      cache-snapshot-memory-size = 26214400
      cache-snapshot-write-cold-duration = "10m0s"
      compact-full-write-cold-duration = "4h0m0s"
      max-series-per-database = 1000000
      max-values-per-tag = 100000
      trace-logging-enabled = false

    [coordinator]
      write-timeout = "10s"
      max-concurrent-queries = 0
      query-timeout = "0s"
      log-queries-after = "0s"
      max-select-point = 0
      max-select-series = 0
      max-select-buckets = 0

    [retention]
      enabled = true
      check-interval = "30m0s"

    [shard-precreation]
      enabled = true
      check-interval = "10m0s"
      advance-period = "30m0s"

    [monitor]
      store-enabled = true
      store-database = "_internal"
      store-interval = "10s"

    [subscriber]
      enabled = true
      http-timeout = "30s"
      insecure-skip-verify = false
      ca-certs = ""
      write-concurrency = 40
      write-buffer-size = 1000

    [http]
      enabled = true
      bind-address = ":8086"
      auth-enabled = false
      log-enabled = true
      write-tracing = false
      pprof-enabled = false
      https-enabled = false
      https-certificate = "/etc/ssl/influxdb.pem"
      https-private-key = ""
      max-row-limit = 10000
      max-connection-limit = 0
      shared-secret = ""
      realm = "InfluxDB"
      unix-socket-enabled = false
      bind-socket = "/var/run/influxdb.sock"

    [[graphite]]
      enabled = true
      bind-address = ":2003"
      database = "graphite"
      retention-policy = ""
      protocol = "tcp"
      batch-size = 5000
      batch-pending = 10
      batch-timeout = "1s"
      consistency-level = "one"
      separator = "."
      udp-read-buffer = 0
      templates = [
       "haystack.buckets.* system.metricGroup.subsystem.application.host.bucket.measurement*",
       "haystack.errors.*  system.metricGroup.subsystem.fqClass.host.lineNumber.measurement*",
       "haystack.*         system.subsystem.application.host.class.measurement*",
      ]
---
apiVersion: v1
kind: Service
metadata:
  labels:
    task: monitoring
    kubernetes.io/cluster-service: "true"
    kubernetes.io/name: monitoring-influxdb
  name: monitoring-influxdb
  namespace: kube-system
spec:
  ports:
   - port: 8086
     targetPort: 8086
  selector:
    k8s-app: monitoring-influxdb
---
apiVersion: v1
kind: Service
metadata:
  labels:
    task: monitoring
    kubernetes.io/cluster-service: "true"
    kubernetes.io/name: monitoring-influxdb
  name: monitoring-influxdb-graphite
  namespace: kube-system
spec:
  type: NodePort
  ports:
   - port: 2003
     name: tcp
     targetPort: 2003
     nodePort: ${graphite_node_port}
  selector:
    k8s-app: monitoring-influxdb
---
apiVersion: apps/v1beta1
kind: StatefulSet
metadata:
  name: monitoring-influxdb
  namespace: kube-system
  labels:
    k8s-addon: monitoring-complete.addons.k8s.io
    k8s-app: monitoring-influxdb
    version: v1
    kubernetes.io/cluster-service: "true"
spec:
  serviceName: monitoring-influxdb
  replicas: 1
  template:
    metadata:
      labels:
        task: monitoring
        k8s-app: monitoring-influxdb
    spec:
      containers:
      - name: influxdb
        image: ${influxdb_image}
        resources:
          # keep request = limit to keep this container in guaranteed class
          limits:
            memory: ${heap_memory_in_mb}Mi
        ports:
          - containerPort: 8083
        volumeMounts:
        - mountPath: /data
          name: influxdb-persistent-storage
        - name: config-volume
          mountPath: "/etc/influxdb"
      nodeSelector:
        ${node_selecter_label}
      volumes:
         - name: config-volume
           configMap:
             name: "influxdb-configmap"
             items:
               - key: "influxdb.toml"
                 path: "config.toml"
  volumeClaimTemplates:
    - metadata:
        name: influxdb-persistent-storage
        annotations:
          volume.beta.kubernetes.io/storage-class: "${influxdb_storage_class}"
      spec:
        storageClassName: "${influxdb_storage_class}"
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: ${influxdb_storage}
