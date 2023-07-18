(ns monkey.blog.api
  (:require [clojure.tools.logging :as log]
            [monkey.blog.persist :as p]
            [reitit.core :as rc]))

(def req-config :monkey.blog/config)

(def storage (comp :storage req-config))
(def area (comp :area :path-params))

(defn- assoc-area [req x]
  (assoc x :area (area req)))

(defn list-entries [req]
  (p/list-entries (storage req) (assoc-area req (:query-params req))))

(defn get-entry [req]
  (log/info "Request:" req)
  (if-let [match (->> (:path-params req)
                      (p/list-entries (storage req))
                      (first))]
    {:status 200
     :body match}
    {:status 404}))

(defn create-entry [req]
  (p/write-entry (storage req) (assoc-area req (:body req))))

(defn update-entry [ctx id opts]
  )

(defn delete-entry [ctx id]
  )
