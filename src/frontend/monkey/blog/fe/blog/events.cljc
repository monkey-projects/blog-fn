(ns monkey.blog.fe.blog.events
  (:require [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.utils :as u]
            [monkey.blog.fe.blog.db :as db]))

(rf/reg-event-fx
 :blog/latest
 (fn [{:keys [db]} _]
   {::martian/request [:get-latest
                       {}
                       [::latest-received]
                       [::latest-failed]]
    :db (a/set-notification db "Loading latest blog entry...")}))

(rf/reg-event-db
 ::latest-received
 (fn [db [_ l]]
   (-> db
       (db/set-latest-entry (:body l))
       (a/clear-notification))))

(rf/reg-event-db
 ::latest-failed
 (fn [db [_ error]]
   (-> db
       (db/set-error (u/extract-error error))
       (a/clear-notification))))
