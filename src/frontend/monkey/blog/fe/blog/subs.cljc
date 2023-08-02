(ns monkey.blog.fe.blog.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.blog.db :as db]))

(rf/reg-sub
 :blog/latest
 (fn [db _]
   (db/latest-entry db)))
