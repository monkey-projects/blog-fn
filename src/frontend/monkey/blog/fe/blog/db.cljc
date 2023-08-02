(ns monkey.blog.fe.blog.db)

(defn set-error [db e]
  (assoc db :error e))

(defn error [db]
  (:error db))

(defn clear-error [db]
  (dissoc db :error))

(defn latest-entry [db]
  (::latest-entry db))

(defn set-latest-entry [db e]
  (assoc db ::latest-entry e))
