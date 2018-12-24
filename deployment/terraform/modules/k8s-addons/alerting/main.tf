module "kubewatch-addon" {
  source = "kubewatch"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.alerting_addons["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubewatch_config_yaml_base64 = "${var.alerting_addons["kubewatch_config_yaml_base64"]}"
  node_selecter_label = "${var.cluster["monitoring-node_selecter_label"]}"
}
