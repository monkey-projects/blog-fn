(ns monkey.blog.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
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
                :parameters {:path {:id s/Str}}}]
       ["" {:name ::create-entry
            :post api/create-entry
            :get api/list-entries
            :parameters {:body s/Any #_{:title s/Str}}}]]]]]
   {:data {:muuntaja mc/instance}}))

(defn make-handler [config]
  (rr/ring-handler (make-router config)
                   (constantly {:status 404 :body "Not found"})))

;; TODO Get config from env
(def handler (make-handler {:storage (p/make-memory-storage)}))

(defn -main [& args]
  (let [opts {:port 8080}]
    (log/info "Starting HTTP server at port" (:port opts))
    (http/run-server handler opts)))
