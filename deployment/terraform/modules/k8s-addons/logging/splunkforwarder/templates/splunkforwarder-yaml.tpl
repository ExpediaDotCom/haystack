apiVersion: v1
kind: ConfigMap
metadata:
  name: splunk-forwarder-config
  namespace: kube-system
data:
  cacert.pem: ...
  client.pem: ...
  limits.conf: ...
  outputs.conf: ...
  server.pem: ...
  inputs.conf: |
    # watch all files in <path>
    [monitor:///var/log/containers/*.log]
    # extract `host` from the first group in the filename
    host_regex = /var/log/containers/(.*)_.*_.*\.log
    # set source type to Kubernetes
    sourcetype = ${cluster_name}
    index = ${splunk_index}
    host = localhost
---
apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
  name: splunk-forwarder
  namespace: kube-system
spec:
  template:
    metadata:
      labels:
        name: splunk-forwarder
        kubernetes.io/cluster-service: "true"
    spec:
      selector:
        matchLabels:
          k8s-app: splunk-forwarder
          kubernetes.io/cluster-service: "true"
      hostNetwork: true
      containers:
      - name: splunk-forwarder
        image: splunk/universalforwarder:6.6.3
        env:
        - name: SPLUNK_START_ARGS
          value: "--accept-license"
        - name: SPLUNK_DEPLOYMENT_SERVER
          value: ${splunk_deployment_server}
        - name: SPLUNK_USER
          value: root
        volumeMounts:
        - mountPath: /var/log
          name: varlog
          readOnly: true
        - mountPath: /var/lib/docker/containers
          name: varlibdockercontainers
          readOnly: true
        - mountPath: /opt/splunk/etc/apps/splunkclouduf/default
          name: config-volume
      terminationGracePeriodSeconds: 30
      volumes:
      - hostPath:
          path: /var/log
        name: varlog
      - hostPath:
          path: /var/lib/docker/containers
        name: varlibdockercontainers
      - name: config-volume
        configMap:
          name: splunk-forwarder-config