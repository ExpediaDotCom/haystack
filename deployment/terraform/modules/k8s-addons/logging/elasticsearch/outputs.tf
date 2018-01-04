output "http_endpoint" {
  value = "http://${local.elasticsearch-name}:${local.elasticsearch-port}"
}
output "host" {
  value = "${local.elasticsearch-name}"
}
output "port" {
  value = "${local.elasticsearch-port}"
}