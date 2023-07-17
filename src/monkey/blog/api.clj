(ns monkey.blog.api
  (:require [clojure.tools.logging :as log]
            [monkey.blog.persist :as p]))

(def storage :storage)
(def area :area)

(defn- make-filter-fn
  "Creates a filter fn according to the argument"
  [area f]
  ;; TODO
  (fn [store]
    (let [s (get store area)]
      (if (some? (:id f))
        [(get s (:id f))]
        s))))

(defn list-entries [{:keys [area] :as ctx} filter]
  (p/list-entries (storage ctx) (make-filter-fn area filter)))

(defn get-entry [ctx id]
  )

(defn create-entry [ctx opts]
  (p/write-entry (storage ctx) (assoc opts :area (area ctx))))

(defn update-entry [ctx id opts]
  )

(defn delete-entry [ctx id]
  )
