terraform {
  backend "local" {
    path = "../state/terraform-k8sAddons.tfstate"
  }
}
