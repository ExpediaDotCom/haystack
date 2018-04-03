terraform {
  backend "local" {
    path = "../state/terraform-infra.tfstate"
  }
}