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
  (if-let [match (->> (get-in req [:parameters :path])
                      (p/list-entries (storage req))
                      (first))]
    {:status 200
     :body match}
    {:status 404}))

(defn create-entry [req]
  (let [entry (->> (get-in req [:parameters :body])
                   (assoc-area req))
        id (p/write-entry (storage req) entry)]
    (log/debug "Created new entry:" (:title entry) "with id" id)
    {:status 201
     :headers {"content-type" "application/json"}
     :body (assoc entry :id id)}))

(defn update-entry [ctx id opts]
  )

(defn delete-entry [ctx id]
  )
