provider "null" {}
provider "template" {}
provider "kubernetes" {
  config_context = "${data.terraform_remote_state.haystack_infrastructure.k8s_cluster_name}"
}
