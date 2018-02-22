kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: kubewatch
rules:
  - apiGroups:
      - ""
    resources:
      - pods
      - services
      - endpoints
      - secrets
    verbs:
      - get
      - list
      - watch
  - apiGroups:
      - extensions
    resources:
      - ingresses
    verbs:
      - get
      - list
      - watch
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: kubewatch 
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: kubewatch
subjects:
- kind: ServiceAccount
  name: kubewatch
  namespace: kube-system
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: kubewatch
  namespace: kube-system
---
kind: Secret
apiVersion: v1
metadata:
  name: kubewatch
  namespace: kube-system
type: Opaque
data:
  .kubewatch.yaml: ${kubewatch_config_yaml_base64}
---
apiVersion: v1
kind: Pod
metadata:
  name: kubewatch
  namespace: kube-system
spec:
  serviceAccountName: kubewatch 
  containers:
  - image: ${kubewatch_image}
    imagePullPolicy: Always
    name: kubewatch
    volumeMounts:
    - name: config-volume
      mountPath: /root
  restartPolicy: Always
  nodeSelector:
    ${node_selecter_label}
  volumes:
  - name: config-volume
    secret:
      secretName: kubewatch

