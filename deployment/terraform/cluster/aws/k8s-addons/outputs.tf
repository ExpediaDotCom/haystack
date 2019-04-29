output "k8s_app_namespace" {
  value = "${module.k8s-addons.k8s_app_namespace}"
}

output "aa_app_namespace" {
  value = "${module.k8s-addons.aa_app_namespace}"
}

output "graphite_hostname" {
  value = "${module.k8s-addons.graphite_hostname}"
}

output "graphite_port" {
  value = "${module.k8s-addons.graphite_port}"
}
