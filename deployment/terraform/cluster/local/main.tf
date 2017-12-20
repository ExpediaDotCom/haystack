//when running locally we expect the machine to have a local k8s cluster using minikube

resource "null_resource" "add_local_domain" {

  provisioner "local-exec" {
    command = " echo '$(minikube ip) ${var.haystack_domain_name}' | tee -a /etc/hosts"
  }
  provisioner "local-exec" {
    command = "sed -i'.bak' '${var.haystack_domain_name}' /etc/hosts"
    when = "destroy"
  }
}


module "k8s-addons" {
  source = "../../modules/k8s-addons"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  traefik_node_port = "${var.traefik_node_port}"
  k8s_app_namespace = "${var.k8s_app_name_space}"
  haystack_domain_name = "${var.haystack_domain_name}"
  k8s_logs_es_url = "elasticsearch"
}

module "haystack-infrastructure" {
  source = "../../modules/haystack-infrastructure/kubernetes"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
}
module "haystack-apps" {
  source = "../../modules/haystack-apps/kubernetes"
  kafka_port = "${module.haystack-infrastructure.kafka_port}"
  elasticsearch_port = "${module.haystack-infrastructure.elasticsearch_port}"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  cassandra_hostname = "${module.haystack-infrastructure.cassandra_hostname}"
  kafka_hostname = "${module.haystack-infrastructure.kafka_hostname}"
  cassandra_port = "${module.haystack-infrastructure.kafka_port}"
  elasticsearch_hostname = "${module.haystack-infrastructure.kafka_port}"
  graphite_hostname = "${module.haystack-infrastructure.kafka_port}"
  k8s_app_namespace = "${module.k8s-addons.k8s_app_namespace}"
}
