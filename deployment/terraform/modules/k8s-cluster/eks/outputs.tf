output "eks_endpoint" {
  value = "${module.aws_eks_cluster.endpoint}"
}

output "kubeconfig" {
  value = "${module.kubectl-config.kubeconfig}"
}


output "kubeconfig_path" {
  value = "${module.kubectl-config.kubeconfig_path}"
}

