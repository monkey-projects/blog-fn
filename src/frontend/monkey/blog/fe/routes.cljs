(ns monkey.blog.fe.routes
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

(defn router [db]
  (::router db))

(defn make-router [base]
  (cond->> [["/" {:name :root}]
            ["/blog"
             [["/new" {:name :blog/new}]]]
            ["/journal"
             [["" {:name :journal}]
              ["/new" {:name :journal/new}]
              ["/search" {:name :journal/search}]]]
            ["/drafts"
             [["" {:name :drafts}]
              ["/new" {:name :drafts/new}]]]
            ["/login" {:name :login}]]
    (not-empty base) (conj [base])
    true (f/router)))

(defn path-for
  "Calculates url for given route"
  [id]
  (rfe/href id))

(defn on-route-change [match history]
  (println "Route changed:" match)
  (rf/dispatch [:route/goto match]))

(defn start! [router]
  (rfe/start! router on-route-change {:use-fragment false}))

(rf/reg-event-fx
 :routing/start
 (fn [{:keys [db]} _]
   (let [base (:base-path db)
         router (make-router base)]
     (println "Creating router for base path" base)
     {:routing/start router
      :db (assoc db ::router router)})))

(rf/reg-fx
 :routing/start
 (fn [router]
   (start! router)))
