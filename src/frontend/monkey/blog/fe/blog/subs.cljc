(ns monkey.blog.fe.blog.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.blog.db :as db]
            [monkey.blog.fe.time :as t]))

(rf/reg-sub
 :blog/latest
 (fn [db _]
   (-> (db/latest-entry db)
       (update :time t/parse-date-time))))
