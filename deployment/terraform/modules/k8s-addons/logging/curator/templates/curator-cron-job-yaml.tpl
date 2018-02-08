apiVersion: v1
kind: ConfigMap
metadata:
  name: curator-es
  namespace: kube-system
  labels:
    app:  curator-es
data:
  curator.yaml: |-
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
    actions:
      1:
        action: delete_indices
        description: >-
          Delete indices older than 45 days (based on index name), for logstash-
          prefixed indices. Ignore the error if the filter does not result in an
          actionable list of indices (ignore_empty_list) and exit cleanly.
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
          timestring: '%Y.%m.%d'
          unit: days
          unit_count: 2
          exclude:
---
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: curator-es
  namespace: kube-system

spec:
  schedule: "0 0 12 * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: curator-es
            image: bobrik/curator:4.0.4
            args:
            - /config/curator.yaml
            volumeMounts:
             - mountPath: /config
               name: config
          restartPolicy: OnFailure
          nodeSelector:
            ${node_selecter_label}
          volumes:
          - name: config
            configMap:
              name: curator-es