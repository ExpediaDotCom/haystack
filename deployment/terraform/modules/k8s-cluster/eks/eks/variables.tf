variable "cluster_name" {}
variable "role_arn" {}
variable "aws_subnet_ids" {
  type = "list"
}
variable "master_security_group_ids" {
  type = "list"
}
variable depends_on {
  default = [],
  type = "list"
}