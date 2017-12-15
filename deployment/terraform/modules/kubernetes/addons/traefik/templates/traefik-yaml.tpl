apiVersion: v1
kind: ConfigMap
metadata:
  name: traefik-config-map
  namespace: kube-system
  labels:
    app: {{ (datasource "config").name }}
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
  name: traefik-cluster-addon
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
  name: traefik-haystack
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: traefik-haystack
subjects:
- kind: ServiceAccount
  name: traefik-haystack
  namespace: kube-system
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: traefik-haystack
  namespace: kube-system
---
kind: Deployment
apiVersion: extensions/v1beta1
metadata:
  name: {{ (datasource "config").name }}
  namespace: kube-system
  labels:
    k8s-app: traefik-haystack
spec:
  replicas: {{ (datasource "config").replicas }}
  selector:
    matchLabels:
      k8s-app: {{ (datasource "config").name }}
  template:
    metadata:
      labels:
        k8s-app: {{ (datasource "config").name }}
        name: {{ (datasource "config").name }}
    spec:
      serviceAccountName: {{ (datasource "config").name }}
      terminationGracePeriodSeconds: 60
{{- if has (datasource "config") "nodeSelector" }}
      nodeSelector:
{{ (datasource "config").nodeSelector | toYAML | strings.Indent 8 }}
{{- end }}
      volumes:
      - name: config
        configMap:
          name: {{ (datasource "config").name }}
{{- if (datasource "config").ssl.enabled }}
      - name: ssl
        secret:
         secretName: {{ (datasource "config").ssl.certsSecretName }}
{{- end }}
      containers:
      - image: {{ (datasource "config").image }}
        name: {{ (datasource "config").name }}
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
{{- if (datasource "config").ssl.enabled }}
         - mountPath: /ssl
           name: ssl
{{- end }}
        resources:
          limits:
            memory: 100Mi
          requests:
            memory: 50Mi
        ports:
        - containerPort: 80
        - containerPort: 8080
        - containerPort: 443
        args:
        - --configfile=/config/traefik.toml
---
apiVersion: v1
kind: Service
metadata:
  name: {{ (datasource "config").name }}
  namespace: kube-system
spec:
  type: NodePort
  ports:
  - port: 80
    name: http
  - port: 443
    name: https
{{- if not (datasource "config").ssl.enabled }}
    targetPort: 80
{{- end }}
    nodePort: {{ (datasource "config").nodePort }}
  selector:
    k8s-app: {{ (datasource "config").name }}
---
apiVersion: v1
kind: Service
metadata:
  name: traefik-web-ui
  namespace: kube-system
spec:
  selector:
    k8s-app: {{ (datasource "config").name }}
  ports:
  - port: 80
    targetPort: 8080
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-haystack-metrictank
  namespace: {{ (datasource "config").namespace }}
  annotations:
    kubernetes.io/ingress.class: traefik
    traefik.frontend.rule.type: PathPrefixStrip
spec:
  rules:
  - host: {{ (datasource "config").hostName }}
    http:
      paths:
       - path: /metrictank-{{ (datasource "config").namespace }}
         backend:
           serviceName: metrictank
           servicePort: 6060
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-haystack-grafana
  namespace: kube-system
  annotations:
    kubernetes.io/ingress.class: traefik
    traefik.frontend.rule.type: PathPrefixStrip
spec:
  rules:
   - host: {{ (datasource "config").hostName }}
     http:
        paths:
         - path: /grafana
           backend:
             serviceName: monitoring-grafana
             servicePort: 80
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-haystack-ui
  namespace: {{ (datasource "config").namespace }}
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
  - host: {{ (datasource "config").haystackUiCname }}
    http:
      paths:
       - path: /
         backend:
           serviceName: haystack-ui
           servicePort: 8080
---
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: traefik-dashboard
  namespace: kube-system
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
  - host: {{ (datasource "config").hostName }}
    http:
      paths:
       - path: /
         backend:
           serviceName: traefik-web-ui
           servicePort: 80