terraform {
  backend "local" {
    path = "../state/terraform-apps.tfstate"
  }
}