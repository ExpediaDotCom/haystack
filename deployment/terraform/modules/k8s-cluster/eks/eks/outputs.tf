output "endpoint" {
  value = "${aws_eks_cluster.master.endpoint}"
}

output "certificate-authority-data" {
  value = "${aws_eks_cluster.master.certificate_authority.0.data}"
}


