apiVersion: v1
kind: ConfigMap
metadata:
  name: curator-es-service-metadata-index-store
  namespace: ${app_namespace}
  labels:
    app:  curator-es-service-metadata-index-store
data:
  curator.yml: |-
    client:
      hosts:
        - ${elasticsearch_host}
      port: ${elasticsearch_port}
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
      loglevel: DEBUG
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
          value: service-metadata-
          exclude:
        - filtertype: age
          source: name
          direction: older
          timestring: "%Y-%m-%d"
          unit: days
          unit_count: 4
          exclude:
---
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: curator-es-service-metadata-index-store
  namespace: ${app_namespace}

spec:
  schedule: "0 */4 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: curator-es-service-metadata-index-store
            image: bobrik/curator:5.4.0
            args:
             - --config
             - /config/curator.yml
             - /config/actions.yml
            volumeMounts:
             - mountPath: /config
               name: config
          restartPolicy: OnFailure
          volumes:
          - name: config
            configMap:
              name: curator-es-service-metadata-index-store
