(ns monkey.blog.api
  (:require [clojure.tools.logging :as log]
            [monkey.blog.persist :as p]
            [reitit.core :as rc]))

(def req-config :monkey.blog/config)

(def storage (comp :storage req-config))
(def area (comp :area :path :parameters))

(defn- assoc-area [req x]
  (assoc x :area (area req)))

(defn list-entries [req]
  (let [f (assoc-area req (:query-params req))]
    (log/debug "Listing entries for" f)
    (let [e (p/list-entries (storage req) f)]
      (if (empty? e)
        {:status 404}
        {:body e
         :status 200}))))

(defn get-entry [req]
  (let [{:keys [id area]} (get-in req [:parameters :path])]
    (if-let [match (->> [(p/read-entry (storage req) id)]
                        (filter (comp (partial = area) :area))
                        (first))]
      {:status 200
       :body match}
      {:status 404})))

(defn get-latest [req]
  (let [area (get-in req [:parameters :path :area])
        match (p/latest-entry (storage req) area)]
    (if (some? match)
      {:status 200
       :body match}
      {:status 404})))

(defn create-entry [req]
  (let [entry (->> (get-in req [:parameters :body])
                   (assoc-area req))
        id (p/write-entry (storage req) entry)]
    (log/debug "Created new entry:" (:title entry) "with id" id)
    {:status 201
     :headers {"content-type" "application/json"}
     :body (assoc entry :id id)}))

(defn update-entry [req]
  (let [{{:keys [area id]} :path :keys [body]} (:parameters req)
        s (storage req)
        match (p/read-entry s id)
        upd (merge match body)]
    (if (and match (= area (:area match)))
      (do
        (p/write-entry s upd)
        {:status 200
         :body upd})
      {:status 404})))

(defn delete-entry [req]
  (let [{:keys [area id]} (get-in req [:parameters :path])]
    (log/debug "Deleting entry" id)
    {:status (if (p/delete-entry (storage req) id)
               204
               404)}))
