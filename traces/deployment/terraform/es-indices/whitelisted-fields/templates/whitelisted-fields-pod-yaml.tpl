apiVersion: v1
kind: ConfigMap
metadata:
  name: whitelist-json
  namespace: ${app_namespace}
data:
  whitelist.json: |-
    {
        "fields": [{
            "name": "error",
            "type": "string",
            "enabled": true,
            "searchContext": "trace"
        }]
    }
---
apiVersion: batch/v1
kind: Job
metadata:
  name: es-whitelist
  namespace: ${app_namespace}
spec:
  template:
    spec:
      containers:
      - name: es-whitelist
        image: yauritux/busybox-curl
        command:
        - curl
        args:
        - -XPUT
        - -H
        - "Content-Type: application/json"
        - -d
        - "@/data/whitelist.json"
        - "http://${elasticsearch_host}:${elasticsearch_port}/reload-configs/indexing-fields/1"
        volumeMounts:
        - mountPath: /data
          name: data
      restartPolicy: OnFailure
      volumes:
        - name: data
          configMap:
            name: whitelist-json