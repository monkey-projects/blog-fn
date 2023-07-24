# Enable required services
locals {
  gcp_services_list = toset([
    "iam.googleapis.com",
    "appengine.googleapis.com",
    "storage.googleapis.com"
  ])
}

resource "google_project_service" "enable_google_apis" {
  for_each = local.gcp_services_list

  project = var.project_id
  service = each.key

  disable_dependent_services = true
}
