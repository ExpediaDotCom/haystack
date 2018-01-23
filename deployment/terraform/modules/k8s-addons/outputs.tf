output "k8s_app_namespace" {
  value = "${module.traefik-addon.k8s_app_namespace}"
}
output "graphite_hostname" {
  value = "${local.graphite_hostname}"
}
output "graphite_port" {
  value = "${local.graphite_port}"
}