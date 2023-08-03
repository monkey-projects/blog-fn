(ns monkey.blog.fe.login.events
  (:require [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.db :as db]
            #?(:cljs [goog.crypt.base64 :refer [encodeString]])))

(defn- update-creds [db f & args]
  (db/set-credentials db (apply f (db/credentials db) args)))

(rf/reg-event-db
 :login/username
 (fn [db [_ u]]
   (update-creds db assoc :username u)))

(rf/reg-event-db
 :login/password
 (fn [db [_ p]]
   (update-creds db assoc :password p)))

(defn- ->base64 [s]
  #?(:cljs (encodeString s)))

(rf/reg-event-fx
 :login
 (fn [{:keys [db]} _]
   (let [{:keys [username password]} (db/credentials db)]
     {::martian/request [:login
                         {:username username
                          :password password}
                         [:login/succeeded]
                         [:login/failed]]})))

(rf/reg-event-db
 :login/succeeded
 (fn [db _]
   (-> db
       (db/set-authenticated true)
       (db/clear-error))))

(rf/reg-event-db
 :login/failed
 (fn [db _]
   (db/set-error db "authentication failed")))

(rf/reg-event-fx
 :login/logoff
 (fn [{:keys [db]} _]
   {::martian/request [:logoff
                       {}
                       [:logoff/success]
                       [:logoff/failed]]
    :db (db/clear-error db)}))

(rf/reg-event-fx
 :logoff/success
 (fn [_ _]
   {:goto "#/"}))

(rf/reg-event-db
 :logoff/failed
 (fn [db _]
   (db/set-error db "logoff failed")))
