(ns monkey.blog.fe.panels
  (:require [re-frame.core :as rf]
            [re-frame.db :as rdb]))

(defn reg-panel [db route-name panel]
  (assoc-in db [::panels route-name] panel))

(rf/reg-sub
 :panels/all
 (fn [db _]
   (::panels db)))
 
(rf/reg-sub
 :panels/current
 :<- [:panels/all]
 :<- [:route/current]
 (fn [[all r] _]
   (get all (get-in r [:data :name]))))
