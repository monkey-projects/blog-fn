(ns monkey.blog.fe.db)

(defn set-current-panel [db p]
  (assoc db :panel/current p))
