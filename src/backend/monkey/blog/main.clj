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
             [ring :as rr]
             [swagger :as rs]]
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
   [["/api" {:middleware [[config-middleware config]]
             :coercion reitit.coercion.schema/coercion}
     ["/entries" {:tags ["entries"]}
      ["/:area" {:parameters {:path {:area s/Str}}}
       ["/:id" {:get
                {:operationId :get-entry
                 :handler api/get-entry}
                :delete
                {:operationId :delete-entry
                 :handler api/delete-entry}
                :put
                {:operationId :update-entry
                 :handler api/update-entry
                 :parameters {:body BlogEntry}}
                :parameters {:path {:id s/Str}}}]
       ["" {:post
            {:operationId :create-entry
             :handler api/create-entry
             :parameters {:body BlogEntry}}
            :get
            {:operationId :list-entries
             :handler api/list-entries}}]]]]
    ["" {:no-doc true}
     ["/health" {:name ::health
                 :get health}]
     ["/swagger.json" {:get
                       {:handler (rs/create-swagger-handler)
                        :swagger {:info {:title "Monkey Projects Blog"}}}}]
     ;; Serve static resources, for dev/test purposes
     ["/site/*" (rr/create-file-handler {:root "resources/public"})]]]
   {:data {:muuntaja mc/instance
           :middleware [rrmm/format-middleware
                        rrc/coerce-request-middleware
                        rrc/coerce-response-middleware]}}))

(defn make-handler [config]
  (rr/ring-handler (make-router config)
                   (rr/create-default-handler)))

;; TODO Get config from env
(def handler (make-handler {:storage (p/make-memory-storage)}))

(defn -main [& args]
  (let [opts (merge {:port 8080} (select-keys env [:port]))]
    (log/info "Starting HTTP server at port" (:port opts))
    (http/run-server handler opts)))
