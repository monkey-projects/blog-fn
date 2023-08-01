(ns monkey.blog.fe.events
  (:require [clojure.string :as cs]
            [monkey.blog.fe.blog.views :as bv]
            [monkey.blog.fe.journal.views :as jv]
            [monkey.blog.fe.location]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [monkey.blog.fe.views :as v]
            [re-frame.core :as rf]))

(defn- strip-index [s]
  (cs/replace s #"/index\.html$" ""))

(defn- register-panels [db]
  (-> db
      (p/reg-panel ::r/root v/home)
      (p/reg-panel ::r/blog--new bv/new-entry)
      (p/reg-panel ::r/journal jv/overview)
      (p/reg-panel ::r/journal--new jv/new-entry)
      (p/reg-panel ::r/journal--edit jv/edit-entry)))

(rf/reg-event-fx
 :initialize-db
 [(rf/inject-cofx :location)]
 (fn [{:keys [location db]} _]
   {:db (-> db
            (assoc :base-path (-> (:path location)
                                  (strip-index)))
            (register-panels))}))
