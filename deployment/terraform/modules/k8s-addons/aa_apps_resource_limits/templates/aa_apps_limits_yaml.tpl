apiVersion: v1
kind: ResourceQuota
metadata:
  name: aa-apps-resource-limits
spec:
  hard:
    requests.cpu: ${cpu_limit}
    requests.memory: ${memory_limit}
    limits.cpu: ${cpu_limit}
    limits.memory: ${memory_limit}