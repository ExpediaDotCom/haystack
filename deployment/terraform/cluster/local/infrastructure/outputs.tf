output "k8s_cluster_name" {
  value = "${var.k8s_minikube_cluster_name}"
}

output "k8s_app_namespace" {
  value = "${module.k8s-addons.k8s_app_namespace}"
}

output "graphite_hostname" {
  value = "${module.k8s-addons.graphite_hostname}"
}

output "graphite_port" {
  value = "${module.k8s-addons.graphite_port}"
}


