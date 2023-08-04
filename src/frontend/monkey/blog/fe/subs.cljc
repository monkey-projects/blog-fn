(ns monkey.blog.fe.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.db :as db]))

(rf/reg-sub
 :login/credentials
 (fn [db _]
   (db/credentials db)))

(rf/reg-sub
 :error
 (fn [db _]
   (db/error db)))

(rf/reg-sub
 :authenticated?
 (fn [db _]
   (db/authenticated? db)))
