output "k8s_app_namespace" {
  value = "${kubernetes_namespace.haystack-app-namespace.metadata.0.name}"
}