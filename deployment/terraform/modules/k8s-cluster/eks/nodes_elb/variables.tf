
variable "aws_elb_subnet" {
type = "list"
}
variable "cluster_name" {}
variable "role_name" {}
variable "nodes_api_security_groups" {
  type = "list"
}
variable "reverse_proxy_port" {}
variable "app-nodes_asg_id" {}
variable "nodes_api_ssl_cert" {}
