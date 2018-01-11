terraform {
  backend "local" {
    path = "terraform-infra.tfstate"
  }
}