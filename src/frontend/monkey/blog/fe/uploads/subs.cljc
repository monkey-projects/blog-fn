(ns monkey.blog.fe.uploads.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.db :as db]))

(rf/reg-sub
 :file/uploads
 (fn [db _]
   (db/uploads db)))

(rf/reg-sub
 :file/last-upload
 (fn [db _]
   (db/last-upload db)))

(rf/reg-sub
 :file/area
 (fn [db _]
   (db/file-area db)))
