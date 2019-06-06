variable "elb_api_security_groups" {
  type = "list"
}
variable "aws_nodes_subnet" {}
variable "k8s_cluster_name" {}
variable "nodes_api_security_groups" {
  type = "list"
}
variable "master-1_asg_id" {}
variable "master-2_asg_id" {}
variable "master-3_asg_id" {}
variable "app-nodes_asg_id" {}
variable "monitoring-nodes_asg_id" {}
variable "graphite_node_port" {}
variable "monitoring_security_groups" {
  type = "list"
}
variable "cluster" {
  type = "map"
}
variable "common_tags" {
  type = "map"
}
variable "nodes_elb_protocol" {}
variable "nodes_elb_port" {}

variable "aws_acm_certificate_arn" {}
