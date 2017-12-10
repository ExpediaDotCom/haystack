//TODO: find a way to source global variables from the aws/global/variables.tf from
variable "kafka_aws_vpc_id" {}
variable "kafka_aws_base_domain_name" {}
variable "kafka_aws_region" {}
variable "kafka_aws_ssh_key" {}
variable "kafka_aws_subnet" {}


variable "es_instance_count" {
  default = 3
}
variable "kafka_broker_count" {
  default = 4
}

variable "kafka_cluster_name" {
  default = "haystack-kafka"
}

variable "kafka_broker_instance_type" {
  default = "m4.xlarge"
}

variable "kafka_base_ami" {
  type = "map"
  default = {
    "ap-northeast-1" = "ami-da9e2cbc",
    "ap-northeast-2" = "ami-1196317f",
    "ap-south-1" = "ami-d5c18eba",
    "ap-southeast-1" = "ami-c63d6aa5"
    "ap-southeast-2" = "ami-ff4ea59d",
    "ca-central-1" = "ami-d29e25b6",
    "eu-central-1" = "ami-bf2ba8d0",
    "eu-west-1" = "ami-1a962263",
    "eu-west-2" = "ami-e7d6c983",
    "sa-east-1" = "ami-286f2a44",
    "us-east-1" = "ami-55ef662f",
    "us-east-2" = "ami-15e9c770",
    "us-west-1" = "ami-a51f27c5",
    "us-west-2" = "ami-bf4193c7"
  }
}