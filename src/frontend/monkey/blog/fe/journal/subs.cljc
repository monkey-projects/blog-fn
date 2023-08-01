(ns monkey.blog.fe.journal.subs
  (:require [monkey.blog.fe.journal.db :as db]
            [re-frame.core :as rf]))

(rf/reg-sub
 ::current-entry
 (fn [db _]
   (db/current db)))
