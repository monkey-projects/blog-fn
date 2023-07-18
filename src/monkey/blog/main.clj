(ns monkey.blog.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [monkey.blog.api :as api]
            [org.httpkit.server :as http]
            [reitit
             [coercion :as rco]
             [core :as rc]
             [ring :as rr]]
            [reitit.coercion.schema]
            [reitit.ring.coercion :as rrc]
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
    ["/api" {:middleware [rrc/coerce-request-middleware
                          [config-middleware config]]
             :coercion reitit.coercion.schema/coercion}
     ["/entries"
      ["/:area" {:parameters {:path {:area s/Str}}}
       ["/:id" {:name ::entry-by-id
                :get api/get-entry
                :parameters {:path {:id s/Uuid}}}]]]]]
   {:compile rco/compile-request-coercers}))

(defn make-handler [config]
  (rr/ring-handler (make-router config)))

(def handler (make-handler {}))

(defn -main [& args]
  (let [opts {:port 8080}]
    (log/info "Starting HTTP server at port" (:port opts))
    (http/run-server handler opts)))
