variable "aws_subnets" {
  type = "list"
}
variable "aws_hosted_zone_id" {}
variable "aws_graphite_host" {}
variable "aws_graphite_port" {}
variable "cluster" {
  type = "map"
}
variable "kafka" {
  type = "map"
}
variable "vpce_whitelisted_accounts" {
  type = "list"
}
variable "common_tags" {
  type = "map"
}
