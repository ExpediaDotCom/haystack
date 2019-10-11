output "kafka_nlb_dns_name" {
  value = "${var.kafka["vpce_enabled"] ? aws_lb.kafka_nlb.dns_name : ""}"
}
