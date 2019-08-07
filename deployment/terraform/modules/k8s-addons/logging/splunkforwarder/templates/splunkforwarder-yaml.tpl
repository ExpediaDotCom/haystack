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
    spec:
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
        - name: SPLUNK_ADD_1
          value: 'monitor /var/log/containers -sourcetype ${cluster_name}-apps -index app -host localhost'
        volumeMounts:
        - mountPath: /var/log
          name: varlog
          readOnly: true
        - mountPath: /var/lib/docker/containers
          name: varlibdockercontainers
          readOnly: true
      nodeSelector:
        ${node_selecter_label}
      terminationGracePeriodSeconds: 30
      volumes:
      - hostPath:
          path: /var/log
        name: varlog
      - hostPath:
          path: /var/lib/docker/containers
        name: varlibdockercontainers
