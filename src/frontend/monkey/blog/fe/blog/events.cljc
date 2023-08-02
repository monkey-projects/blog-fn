(ns monkey.blog.fe.blog.events
  (:require [re-frame.core :as rf]
            #_[ajax.core :as ajax]
            #_[ajax.edn :as edn]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.utils :as u]
            [monkey.blog.fe.blog.db :as db]))

(rf/reg-event-fx
 :blog/latest
 (fn [{:keys [db]} _]
   {:http-xhrio {} #_{:method :get
                   :uri "api/blog/latest"
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::latest-received]
                   :on-failure [::latest-failed]}
    :db (a/set-notification db "Loading latest blog entry...")}))

(rf/reg-event-db
 ::latest-received
 (fn [db [_ l]]
   (-> db
       (db/set-latest-entry l)
       (a/clear-notification))))

(rf/reg-event-db
 ::latest-failed
 (fn [db [_ error ]]
   (-> db
       (db/set-error (u/extract-error error))
       (a/clear-notification))))
