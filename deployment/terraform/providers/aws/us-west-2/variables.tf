//TODO: find a way to source global variables from the aws/global/variables.tf from
variable "es_instance_count" {
  default = 3
}
variable "es_master_count" {
  default = 2
}
variable "aws_vpc_id" {
  default = "vpc-de7925b9"
}
variable "aws_subnet" {
  default = "subnet-33f0ea6b"
}
