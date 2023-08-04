(ns monkey.blog.fe.db
  (:require [monkey.blog.fe.alerts :as a]))

(defn current-panel [db]
  (::current-panel db))

(defn set-current-panel [db p & [params]]
  (assoc db ::current-panel {:panel p
                             :params params}))

(defn credentials [db]
  (:login/credentials db))

(defn set-credentials [db creds]
  (assoc db :login/credentials creds))

(defn authenticated? [db]
  (true? (:login/authenticated? db)))

(defn set-authenticated [db a]
  (assoc db :login/authenticated? a))

(defn set-error [db e]
  (a/set-error db e))

(defn clear-error [db]
  (a/clear-error db))

(defn error [db]
  (a/error db))

(defn set-uploads [db u]
  (assoc db :file/uploads u))

(defn uploads [db]
  (get db :file/uploads))

(defn set-last-upload [db u]
  (assoc db :file/last-upload u))

(defn last-upload [db]
  (:file/last-upload db))

(defn set-file-area [db a]
  (assoc db :file/area a))

(defn file-area [db]
  (:file/area db))
