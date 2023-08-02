(ns monkey.blog.fe.drafts.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.drafts.db :as db]))

(rf/reg-sub
 :drafts
 (fn [db _]
   (db/drafts db)))

(rf/reg-sub
 :draft/current
 (fn [db _]
   (db/current-draft db)))
