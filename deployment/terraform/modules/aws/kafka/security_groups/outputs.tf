output "worker_security_group_ids" {
  value = [
    "${aws_security_group.workers-haystack-kafka.id}"]
}

output "broker_security_group_ids" {
  value = [
    "${aws_security_group.brokers-haystack-kafka.id}"]
}