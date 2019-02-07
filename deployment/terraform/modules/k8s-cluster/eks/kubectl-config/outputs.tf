output "kubeconfig" {
  value = "${local.kubeconfig}"
}

output "config_map_aws_auth" {
  value = "${local.config_map_aws_auth}"
}
output "kubeconfig_path" {
  value = "${local.rendered_kube_config_path}"
  depends_on = ["null_resource.config_map_aws_auth"]

}
