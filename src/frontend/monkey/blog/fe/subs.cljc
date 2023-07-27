(ns monkey.blog.fe.subs
  "Common subscriptions, used throughout the app"
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :user
 (fn [_ _]
   ;; TODO Get credentials somehow
   nil))

(rf/reg-sub
 :authenticated?
 :<- [:user]
 (fn [user _]
   (some? user)))

(rf/reg-sub
 :panel/current
 (fn [db _]
   (:panel/current db)))
