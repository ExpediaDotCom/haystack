variable "k8s_cluster_name" {}
variable "graphite_node_port" {}
variable "cluster" {
  type = "map"
}
variable "nodes_elb_port" {}
variable "common_tags" {
  type = "map"
  
}