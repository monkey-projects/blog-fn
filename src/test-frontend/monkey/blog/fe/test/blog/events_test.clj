(ns monkey-blog.fe.blog.events-test
  (:require [midje.sweet :refer :all]
            [re-frame
             [db :refer [app-db]]
             [core :as rf]]            
            [monkey-blog.fe.blog
             [db :as db]
             [events :as e]]
            [monkey-blog.fe
             [alerts :as a]
             [helpers :as h]]))

(facts "about `blog/latest`"
       (fact "loads latest from backend"
             (let [e (h/catch-fx :http-xhrio)]
               (rf/dispatch-sync [:blog/latest])
               @e => (just [(contains {:method :get
                                       :uri "api/blog/latest"})])))

       (fact "sets notification"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:blog/latest])
             (a/notification @app-db) => string?))

(facts "about `latest-received`"
       (fact "sets entry in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::e/latest-received ..latest..])
             (db/latest-entry @app-db) => ..latest..)

       (fact "clears notification"
             (reset! app-db (a/set-notification {} "test notification")) => truthy
             (rf/dispatch-sync [::e/latest-received ..latest..])
             (a/notification @app-db) => nil?))

(facts "about `latest-failed`"
       (fact "sets error"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::e/latest-failed "test error"])
             (db/error @app-db) => #"test error")

       (fact "clears notification"
             (reset! app-db (a/set-notification {} "test notification")) => truthy
             (rf/dispatch-sync [::e/latest-failed "test error"])
             (a/notification @app-db) => nil?))
