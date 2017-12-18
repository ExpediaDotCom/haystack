module "kaystack-app-deployments" {
  source = "../../modules/kubernetes"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
}

