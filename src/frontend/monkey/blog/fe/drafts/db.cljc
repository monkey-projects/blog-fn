(ns monkey.blog.fe.drafts.db
  (:require [monkey.blog.fe.db :as cdb]))

(defn set-error [db e]
  (cdb/set-error db e))

(defn set-drafts [db d]
  (assoc db ::drafts d))

(defn drafts [db]
  (::drafts db))

(defn update-drafts [db f & args]
  (apply update db ::drafts f args))

(defn draft-by-id [db id]
  (->> (drafts db)
       (filter (comp (partial = id) :id))
       (first)))

(defn set-current-draft [db d]
  (assoc db ::current d))

(defn current-draft [db]
  (::current db))
