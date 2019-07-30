variable "k8s_nodes_iam-role_arn" {}
variable "cluster" {
  type = "map"
}
variable "es_spans_index" {
  type = "map"
}
variable "aws_subnet" {}
variable "haystack_index_store_es_version" {
  default = "6.0"
}
variable "common_tags" {
  type = "map"
}