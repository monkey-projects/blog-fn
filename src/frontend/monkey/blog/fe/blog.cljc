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

(rf/reg-event-fx
 :blog/load-latest
 (fn [db _]
   ;; TODO Send actual request to backend
   {:dispatch [:blog/load-latest--loaded {:id "latest"
                                          :title "Latest blog entry"
                                          :contents "This is the latest blog entry."}]}))

(rf/reg-event-db
 :blog/load-latest--loaded
 (fn [db [_ e]]
   (println "Latest blog entry loaded:" e)
   (set-latest db e)))
