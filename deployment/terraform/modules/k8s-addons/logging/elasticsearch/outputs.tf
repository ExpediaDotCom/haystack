output "http_endpoint" {
  value = "http://${local.elasticsearch-name}:${local.elasticsearch-port}"
}