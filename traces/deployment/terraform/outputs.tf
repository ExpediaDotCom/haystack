output "reader_hostname" {
  value = "${module.trace-reader.hostname}"
}

output "reader_port" {
  value = "${module.trace-reader.service_port}"
}