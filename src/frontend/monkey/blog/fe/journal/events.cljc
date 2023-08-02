(ns monkey.blog.fe.journal.events
  (:require [monkey.blog.fe.journal.db :as db]
            [re-frame.core :as rf]))

(rf/reg-event-db
 ::prop-changed
 (fn [db [_ k v]]
   (db/update-current db assoc k v)))
