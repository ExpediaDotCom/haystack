output "metrictank_hostname" {
  value = "${local.external_metric_tank_enabled == "true" ? var.metrictank["external_hostname"] : module.metrictank.metrictank_hostname}"
}

output "metrictank_port" {
  value = "${local.external_metric_tank_enabled == "true" ? var.metrictank["external_port"] : module.metrictank.metrictank_port}"
}