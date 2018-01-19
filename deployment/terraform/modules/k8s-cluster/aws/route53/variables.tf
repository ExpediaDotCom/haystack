variable "master_elb_dns_name" {}
variable "nodes_elb_dns_name" {}
variable "k8s_cluster_name" {}
variable "aws_hosted_zone_id" {}

variable "kubectl_executable_name" {}


variable "haystack_ui_cname" {}
variable "k8s_dashboard_cname" {}
variable "k8s_dashboard_cname_enabled" {

}

variable "metrics_cname" {}
variable "metrics_cname_enabled" {}

variable "logs_cname" {}
variable "logs_cname_enabled" {}

