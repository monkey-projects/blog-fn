(ns monkey.blog.api
  (:require [clojure.tools.logging :as log]
            [monkey.blog.persist :as p]))

(def storage :storage)
(def area :area)

(defn- assoc-area [ctx x]
  (assoc x :area (area ctx)))

(defn list-entries [ctx filter]
  (p/list-entries (storage ctx) (assoc-area ctx filter)))

(defn get-entry [ctx id]
  (->> {:id id}
       (assoc-area ctx)
       (p/list-entries (storage ctx))
       (first)))

(defn create-entry [ctx opts]
  (p/write-entry (storage ctx) (assoc-area ctx opts)))

(defn update-entry [ctx id opts]
  )

(defn delete-entry [ctx id]
  )
