apiVersion: v1
clusters:
- cluster:
    server: ${aws_eks_cluster_endpoint}
    certificate-authority-data: ${certificate-authority-data}
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: aws
  name: aws
current-context: aws
kind: Config
preferences: {}
users:
- name: aws
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1alpha1
      command: ${aws_iam_authenticator}
      args:
        - "token"
        - "-i"
        - "${cluster_name}"


