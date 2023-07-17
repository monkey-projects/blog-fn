(ns monkey.blog.persist
  "Interface code for persistent storage")

(defprotocol Storage
  (list-entries [s f])
  (read-entry [s id])
  (write-entry [s e])
  (delete-entry [s id]))

;; In memory implementation, used for development/testing
(defrecord MemoryStorage [store]
  Storage
  (list-entries [ms f]
    (f @store))

  (write-entry [ms {:keys [area] :as e}]
    (let [id (random-uuid)]
      (swap! store assoc-in [area id] e)
      id))

  (read-entry [ms id]))

(defn make-memory-storage []
  (->MemoryStorage (atom nil)))
