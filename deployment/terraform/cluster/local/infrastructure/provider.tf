provider "null" {}
provider "template" {}
provider "kubernetes" {
  config_context = "${var.kubectl_context_name}"
}