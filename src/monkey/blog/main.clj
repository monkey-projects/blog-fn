(ns monkey.blog.main
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as http]
            [reitit
             [core :as rc]
             [ring :as rr]]))

(defn- health [_]
  {:status 200})

(def router
  (rr/router
   [["/health" {:name ::health
                :get health}]]))

(def handler (rr/ring-handler router))

(defn -main [& args]
  (let [opts {:port 8080}]
    (log/info "Starting HTTP server at port" (:port opts))
    (http/run-server router opts)))
