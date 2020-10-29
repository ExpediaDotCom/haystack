apiVersion: v1
kind: ConfigMap
metadata:
  name: curator-es
  namespace: kube-system
  labels:
    app:  curator-es
data:
  curator.yaml: |
    # curator.yaml
    action: delete_indices
    description: "Delete selected indices"
    options:
      timeout_override: 300
      continue_if_exception: False
---
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: curator-es
spec:
  schedule: "*/1 * * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: curator-es
            image: ${curator_image}
            args:
            - --config
            - /config/curator.yaml
            volumeMounts:
             - mountPath: /config
               name: config
          restartPolicy: OnFailure
          volumes:
          - name: config
            configMap:
              name: curator-es