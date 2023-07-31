(ns monkey.blog.fe.routing
  (:require [re-frame.core :as rf]
            [reitit.frontend :as f]
            [reitit.frontend.easy :as rfe]))

(rf/reg-sub
 :route/current
 (fn [db _]
   (:route/current db)))

(rf/reg-event-db
 :route/goto
 (fn [db [_ match]]
   (assoc db :route/current match)))

(defn make-router [base]
  (cond->> [["" ::root]
            ["/journal" ::journal]
            ["/login" ::login]]
    (not-empty base) (conj [base])
    true (f/router)))

(defn on-route-change [match history]
  (println "Route changed:" match)
  (rf/dispatch [:route/goto match]))

(defn start! [base]
  (println "Creating router for base path" base)
  (rfe/start! (make-router base) on-route-change {:use-fragment false}))

(rf/reg-event-fx
 :routing/start
 (fn [{:keys [db]} _]
   {:routing/start (:base-path db)}))

(rf/reg-fx
 :routing/start
 (fn [base]
   (start! base)))
