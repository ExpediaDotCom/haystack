variable "elb_api_security_groups" {
  type = "list"
}
variable "aws_elb_subnet" {}
variable "aws_nodes_subnet" {}
variable "k8s_cluster_name" {}
variable "haystack_cluster_name" {}
variable "nodes_api_security_groups" {
  type = "list"
}
variable "reverse_proxy_port" {}
variable "master-1_asg_id" {}
variable "master-2_asg_id" {}
variable "master-3_asg_id" {}
variable "app-nodes_asg_id" {}
variable "monitoring-nodes_asg_id" {}
variable "graphite_node_port" {}
variable "monitoring_security_groups" {
  type = "list"
}