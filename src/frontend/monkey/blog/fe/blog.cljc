(ns monkey.blog.fe.blog
  "Blog related functionality"
  (:require [re-frame.core :as rf]))

(defn latest [db]
  (::latest db))

(defn set-latest [db e]
  (assoc db ::latest e))

(rf/reg-sub
 :blog/latest
 (fn [db _]
   (latest db)))

(rf/reg-event-db
 :blog/load-latest--loaded
 (fn [db [_ e]]
   (set-latest db e)))
