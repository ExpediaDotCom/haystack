apiVersion: v1
kind: ConfigMap
metadata:
  name: ${traefik_name}
  namespace: kube-system
  labels:
    app:  ${traefik_name}
data:
  traefik.toml: |
    # traefik.toml
    logLevel = "INFO"
    defaultEntryPoints = ["http"]
    [entryPoints]
      [entryPoints.http]
      address = ":80"
      compress = false
    [kubernetes]
    [web]
    address = ":8080"
---
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: ${traefik_name}
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
  name: ${traefik_name}
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: ${traefik_name}
subjects:
- kind: ServiceAccount
  name: ${traefik_name}
  namespace: kube-system
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${traefik_name}
  namespace: kube-system
---
kind: Deployment
apiVersion: extensions/v1beta1
metadata:
  name: ${traefik_name}
  namespace: kube-system
  labels:
    k8s-app: traefik-haystack
spec:
  replicas: ${traefik_replicas}
  selector:
    matchLabels:
      k8s-app: ${traefik_name}
  template:
    metadata:
      labels:
        k8s-app:  ${traefik_name}
        name:  ${traefik_name}
    spec:
      serviceAccountName: ${traefik_name}
      terminationGracePeriodSeconds: 60
      volumes:
      - name: config
        configMap:
          name: ${traefik_name}
      containers:
      - image: ${traefik_image}
        name: ${traefik_name}
        livenessProbe:
          tcpSocket:
            port: 80
          failureThreshold: 3
          initialDelaySeconds: 10
          periodSeconds: 10
          successThreshold: 1
          timeoutSeconds: 2
        volumeMounts:
         - mountPath: /config
           name: config
        resources:
          limits:
            memory: 512Mi
          requests:
            memory: 128Mi
        ports:
        - containerPort: 80
        - containerPort: 8080
        - containerPort: 443
        args:
        - --configfile=/config/traefik.toml
      nodeSelector:
        ${node_selecter_label}
---
apiVersion: v1
kind: Service
metadata:
  name: ${traefik_name}
  namespace: kube-system
spec:
  type: NodePort
  ports:
  - port: 80
    name: http
    targetPort: 80
    nodePort: ${node_port}
  selector:
    k8s-app: ${traefik_name}
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-haystack-ui
  namespace: ${k8s_app_namespace}
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
  - host: ${haystack_ui_cname}
    http:
      paths:
       - path: /
         backend:
           serviceName: haystack-ui
           servicePort: 80
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-http-span-collector
  namespace: ${k8s_app_namespace}
  annotations:
    kubernetes.io/ingress.class: traefik
    traefik.frontend.rule.type: PathPrefix
spec:
  rules:
  - host: ${haystack_ui_cname}
    http:
      paths:
       - path: /span
         backend:
           serviceName: http-span-collector
           servicePort: 80
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-pitchfork
  namespace: ${k8s_app_namespace}
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
  - host: ${haystack_ui_cname}
    http:
      paths:
       - path: /zipkin
         backend:
           serviceName: pitchfork
           servicePort: 80
---