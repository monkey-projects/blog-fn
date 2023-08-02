(ns monkey.blog.fe.routes
;;   (:require [secretary.core :as s :refer-macros [defroute]]
;;             [re-frame.core :as rf]
;;             [goog.events :as events])
;;   (:import [goog History]
;;            [goog.history Event EventType]))

;; (defroute home "/" []
;;   (rf/dispatch [:route/selected :home]))

;; (defroute journal-latest "/journal" []
;;   (println "Navigating to journal overview")
;;   (rf/dispatch [:route/selected :journal]))

;; (defroute edit-journal "/journal/edit/:id" [id]
;;   (println "Editing journal:" id)
;;   (rf/dispatch [:journal/edit id]))

;; (defroute view-journal "/journal/view/:id" [id]
;;   (rf/dispatch [:journal/view id]))

;; (defroute new-journal "/journal/new" []
;;   (rf/dispatch [:journal/new]))

;; (defroute search-journal "/journal/search" []
;;   (rf/dispatch [:route/selected :journal/search]))

;; (defroute journal "/journal/:month" [month]
;;   (println "Showing month:" month)
;;   (rf/dispatch [:journal/set-period month]))

;; (defroute upload "/upload" []
;;   (rf/dispatch [:route/selected :uploads]))

;; (defroute upload-list "/upload/list" []
;;   (rf/dispatch [:route/selected :uploads/list]))

;; (defroute drafts "/drafts" []
;;   (rf/dispatch [:route/selected :drafts]))

;; (defroute edit-draft "/drafts/edit/:id" [id]
;;   (rf/dispatch [:draft/edit id]))

;; (defroute new-draft "/drafts/new" []
;;   (rf/dispatch [:draft/new]))

;; (defn- evt-handler [^Event evt]
;;   (s/dispatch! (.-token evt)))

;; (defn enable-history! []
;;   ;; Listen to history events and send to secretary
;;   (doto (History.)
;;     (events/listen EventType.NAVIGATE evt-handler)
;;     (.setEnabled true)))

;; (defn goto! [r]
;;   (println "Navigating to:" r)
;;   (set! (.-href js/location) r))

;; (rf/reg-fx
;;  :goto
;;  (fn [r]
;;    (goto! r)))
  
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
  (cond->> [["" {:name ::root}]
            ["/blog"
             [["/new" {:name ::blog--new}]]]
            ["/journal"
             [["" {:name ::journal}]
              ["/new" {:name ::journal--new}]
              ["/search" {:name ::journal--search}]]]
            ["/drafts"
             [["" {:name ::drafs}]
              ["/new" {:name ::drafs--new}]]]
            ["/login" {:name ::login}]]
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
