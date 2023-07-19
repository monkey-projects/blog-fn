(ns monkey.blog.persist
  "Interface code for persistent storage")

(defprotocol Storage
  (list-entries [s f])
  (read-entry [s id])
  (write-entry [s e])
  (delete-entry [s id]))

(defn- make-filter-fn
  "Creates a filter fn according to the argument, used by memory implementation"
  [{:keys [area] :as f}]
  ;; TODO
  (fn [store]
    (let [s (get store area)]
      (if (some? (:id f))
        [(get s (:id f))]
        (map (fn [[id v]]
               (assoc v :id id))
             s)))))

;; In memory implementation, used for development/testing
(defrecord MemoryStorage [store]
  Storage
  (list-entries [ms f]
    ((make-filter-fn f) @store))

  (write-entry [ms {:keys [area] :as e}]
    (let [id (str (random-uuid))]
      (swap! store assoc-in [area id] e)
      id))

  (read-entry [ms id]))

(defn make-memory-storage []
  (->MemoryStorage (atom nil)))
