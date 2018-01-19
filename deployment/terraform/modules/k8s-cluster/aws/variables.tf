variable "aws_ssh_key" {}
variable "s3_bucket_name" {}
variable "aws_vpc_id" {}
variable "aws_nodes_subnet" {}
variable "aws_utility_subnet" {}
variable "aws_hosted_zone_id" {}
variable "aws_domain_name" {}

variable "node_instance_type" {}
variable "node_instance_count" {}
variable "master_instance_type" {}

variable "reverse_proxy_port" {}
variable "haystack_ui_cname" {}
variable "k8s_dashboard_cname" {}
variable "k8s_dashboard_cname_enabled" {}
variable "metrics_cname" {}
variable "metrics_cname_enabled" {}
variable "logs_cname" {}
variable "logs_cname_enabled" {}
variable "kops_executable_name" {}
variable "kubectl_executable_name" {}
variable "haystack_cluster_name" {}
variable "k8s_version" {
  default = "1.8.4"
}
variable "node_ami" {
  default = "ami-06a57e7e"
}

variable "master_ami" {
  default = "ami-06a57e7e"
}
