(ns monkey.blog.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [config.core :refer [env]]
            [monkey.blog
             [api :as api]
             [persist :as p]]
            [muuntaja.core :as mc]
            [org.httpkit.server :as http]
            [reitit
             [coercion :as rco]
             [core :as rc]
             [ring :as rr]]
            [reitit.coercion.schema]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [schema.core :as s]))

(defn- health [_]
  ;; For now, always successful
  {:status 200})

(defn config-middleware
  "Adds configuration to the request"
  [handler config]
  (fn [req]
    (handler (assoc req :monkey.blog/config config))))

(s/defschema BlogEntry
  {(s/optional-key :title) (s/maybe s/Str)
   :contents s/Str})
  
(defn make-router [config]
  (rr/router
   [["/health" {:name ::health
                :get health}]
    ["/api" {:middleware [rrmm/format-middleware
                          rrc/coerce-request-middleware
                          rrc/coerce-response-middleware
                          [config-middleware config]]
             :coercion reitit.coercion.schema/coercion}
     ["/entries"
      ["/:area" {:parameters {:path {:area s/Str}}}
       ["/:id" {:name ::entry-by-id
                :get api/get-entry
                :delete api/delete-entry
                :put {:handler api/update-entry
                      :parameters {:body BlogEntry}}
                :parameters {:path {:id s/Str}}}]
       ["" {:name ::create-entry
            :post {:handler api/create-entry
                   :parameters {:body BlogEntry}}
            :get api/list-entries}]]]]]
   {:data {:muuntaja mc/instance}}))

(defn make-handler [config]
  (rr/ring-handler (make-router config)
                   (constantly {:status 404 :body "Not found"})))

;; TODO Get config from env
(def handler (make-handler {:storage (p/make-memory-storage)}))

(defn -main [& args]
  (let [opts (merge {:port 8080} (select-keys env [:port]))]
    (log/info "Starting HTTP server at port" (:port opts))
    (http/run-server handler opts)))
