# Define the app.
# Versions are managed by the build pipeline.
resource "google_app_engine_application" "app" {
  project     = var.project_id
  location_id = var.location_id
}

resource "google_app_engine_domain_mapping" "test_domain" {
  domain_name = "test.neirynck.org"
  ssl_settings {
    ssl_management_type = "AUTOMATIC"
  }
}
