(ns monkey.blog.fe.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :authenticated?
 (fn [_ _]
   false))

(rf/reg-sub
 :panel/current
 (fn [db _]
   (:panel/current db)))
