(ns monkey.blog.fe.subs
  "Common subscriptions, used throughout the app"
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :user
 (fn [_ _]
   ;; TODO Get credentials somehow
   {:username "admin"
    :roles #{:admin}}))

(rf/reg-sub
 :authenticated?
 :<- [:user]
 (fn [user _]
   (some? user)))
