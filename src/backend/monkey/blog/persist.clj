(ns monkey.blog.persist
  "Interface code for persistent storage")

(defprotocol Storage
  "Storage abstraction.  Used to store/retrieve blog entries."
  (list-entries [s f])
  (read-entry [s id])
  (write-entry [s e])
  (delete-entry [s id] "Delete an entry.  Returns `id` if the entry existed, `nil` otherwise."))

(defn- make-filter-fn
  "Creates a filter fn according to the argument, used by memory implementation"
  [{:keys [area] :as f}]
  ;; TODO
  (fn [store]
    (->> (if (some? (:id f))
           [(get store (:id f))]
           (vals store))
         (filter (comp (partial = area) :area)))))

;; In memory implementation, used for development/testing
(defrecord MemoryStorage [store]
  Storage
  (list-entries [ms f]
    ((make-filter-fn f) @store))

  (write-entry [ms {:keys [id area] :as e}]
    (let [id (or id (str (random-uuid)))]
      (swap! store assoc id (assoc e :id id))
      id))

  (read-entry [ms id]
    (get @store id))

  (delete-entry [ms id]
    (when (some? (read-entry ms id))
      (swap! store dissoc id)
      id)))

(defn make-memory-storage []
  (->MemoryStorage (atom {})))

(defn latest-entry
  "Retrieves latest entry.  This should be moved into the protocol as it
   can be made much more efficient depending on storage implementation."
  [s area]
  (->> (list-entries s {:area area})
       (sort-by :time)
       (last)))
