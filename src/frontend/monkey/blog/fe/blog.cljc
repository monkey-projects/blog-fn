(ns monkey.blog.fe.blog
  "Blog related functionality"
  (:require [martian.re-frame :as martian]
            [monkey.blog.fe.alerts :as alerts]
            [re-frame.core :as rf]))

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
 (fn [ctx _]
   {:dispatch
    [::martian/request
     :list-entries
     {:area "blog"}
     [:blog/load-latest--loaded]
     [:blog/load-latest--failed]]}))

(rf/reg-event-db
 :blog/load-latest--loaded
 (fn [db [_ e]]
   (println "Latest blog entry loaded:" e)
   (set-latest db (last (:body e)))))

(rf/reg-event-db
 :blog/load-latest--failed
 (fn [db [_ {:keys [status body]}]]
   (cond-> db
     (= 404 status) (set-latest {})
     (not= 404 status) (-> (set-latest nil)
                           (alerts/set-error body)))))
