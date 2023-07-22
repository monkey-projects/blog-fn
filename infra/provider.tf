provider "google" {
  project = var.project_id
  region  = var.location_id
  zone    = "europe-west1-b"
}
