apiVersion: v1
kind: ConfigMap
metadata:
  name: curator-es-logs
  namespace: kube-system
  labels:
    app:  curator-es-logs
data:
  curator.yml: |-
    client:
      hosts:
        - ${elasticsearch_host}
      port: 9200
      url_prefix:
      use_ssl: False
      certificate:
      client_cert:
      client_key:
      aws_key:
      aws_secret_key:
      aws_region:
      ssl_no_validate: False
      http_auth:
      timeout: 30
      master_only: False
    logging:
      loglevel: INFO
      logfile:
      logformat: default
      blacklist: ['elasticsearch', 'urllib3']
  actions.yml: |-
    actions:
      1:
        action: delete_indices
        options:
          ignore_empty_list: True
          timeout_override:
          continue_if_exception: False
          disable_action: False
        filters:
        - filtertype: pattern
          kind: prefix
          value: logstash-
          exclude:
        - filtertype: age
          source: name
          direction: older
          timestring: "%Y.%m.%d"
          unit: days
          unit_count: 2
          exclude:
---
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: curator-es-logs
  namespace: kube-system

spec:
  schedule: "0 0 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: curator-es-logs
            image: ${curator_image}
            args:
             - --config
             - /config/curator.yml
             - /config/actions.yml
            volumeMounts:
             - mountPath: /config
               name: config
          restartPolicy: OnFailure
          nodeSelector:
            ${node_selecter_label}
          volumes:
          - name: config
            configMap:
              name: curator-es-logs
