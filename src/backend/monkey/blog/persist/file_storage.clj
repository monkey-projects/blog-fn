(ns monkey.blog.persist.file-storage
  "Persistent store that uses plain json files as backend.  This can be used
   for local development purposes."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as cs]
            [monkey.blog.persist :as p]))

(defn- ->file-filter [f]
  (reify java.io.FileFilter
    (accept [_ path]
      ;; TODO
      true)))

(defn- id->area [id]
  (some-> id
          (cs/split #"#")
          (first)))

(defn- id->file [st id]
  (io/file (:dir st) (id->area id) (str id ".json")))

(defrecord FileStorage [dir]
  p/Storage
  (list-entries [st {:keys [area] :as f}]
    (->> (.listFiles (io/file dir area) (->file-filter f))
         (seq)
         (map (comp json/read-json slurp))))
  
  (read-entry [st id]
    (let [f (id->file st id)]
      (when (.canRead f)
        (json/read-json (slurp f)))))
  
  (write-entry [st {:keys [area] :as e}]
    (let [id (or (:id e) (str area "#" (random-uuid)))]
      (.mkdirs (io/file dir area))
      (spit (id->file st id) (json/write-str (assoc e :id id)))
      id))
  
  (delete-entry [st id]
    (when (.delete (id->file st id))
      id)))

(defn make-file-storage [dir]
  (let [f (io/file dir)]
    (.mkdirs f)
    (->FileStorage f)))
