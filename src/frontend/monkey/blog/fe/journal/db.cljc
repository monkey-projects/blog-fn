(ns monkey.blog.fe.journal.db)

(defn set-current [db e]
  (assoc db ::current e))

(defn update-current [db f & args]
  (apply update db ::current f args))

(defn clear-current [db]
  (dissoc db ::current))

(defn current [db]
  (::current db))
