variable "aws_subnet" {}
variable "aws_hosted_zone_id" {}
variable "graphite_host" {}
variable "graphite_port" {}
variable "cluster" {
  type = "map"
}
variable "cassandra_spans_backend" {
  type = "map"
}