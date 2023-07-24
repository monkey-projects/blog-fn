resource "google_service_account" "circleci_account" {
  account_id = "circleci"
  display_name = "CircleCI"
  description = "Used by CircleCI build pipelines"

  depends_on = [
    google_project_service.enable_google_apis["iam.googleapis.com"]
  ]
}

resource "google_project_iam_member" "gae_api" {
  project = google_service_account.circleci_account.project
  role    = "roles/compute.networkUser"
  member  = "serviceAccount:${google_service_account.circleci_account.email}"
}

resource "google_project_iam_member" "storage_viewer" {
  project = google_service_account.circleci_account.project
  role    = "roles/storage.objectAdmin"
  member  = "serviceAccount:${google_service_account.circleci_account.email}"
}

resource "google_project_iam_member" "appengine_deployer" {
  project = google_service_account.circleci_account.project
  role    = "roles/appengine.deployer"
  member  = "serviceAccount:${google_service_account.circleci_account.email}"
}

resource "google_project_iam_member" "iam_actas" {
  project = google_service_account.circleci_account.project
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:${google_service_account.circleci_account.email}"
}

resource "google_project_iam_member" "cloudbuild_editor" {
  project = google_service_account.circleci_account.project
  role    = "roles/cloudbuild.builds.editor"
  member  = "serviceAccount:${google_service_account.circleci_account.email}"
}

resource "google_project_iam_member" "appengine_service_admin" {
  project = google_service_account.circleci_account.project
  role    = "roles/appengine.serviceAdmin"
  member  = "serviceAccount:${google_service_account.circleci_account.email}"
}

resource "google_service_account_key" "circleci" {
  service_account_id = google_service_account.circleci_account.id
}

# In order to get the json file for the key, run this command
# terraform output circleci_private_key | jq -r | base64 -d > key.json
output "circleci_private_key" {
  value = google_service_account_key.circleci.private_key
  sensitive = true
}
