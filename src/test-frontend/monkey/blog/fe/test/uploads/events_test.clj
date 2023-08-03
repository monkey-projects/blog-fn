(ns monkey.blog.fe.uploads.events-test
  (:require [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [monkey.blog.fe.uploads.events :as sut]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.helpers :refer :all]
            [midje.sweet :refer :all]))

(facts "about `file/list-uploads`"
       (fact "sends request to backend"
             (let [e (catch-fx :http-xhrio)]
               (rf/dispatch-sync [:file/list-uploads])
               @e => (just [(contains {:method :get
                                       :uri "api/uploads"})]))))

(facts "about `file/list-uploads-failed`"
       (fact "sets error"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:file/list-uploads-failed "test error"])
             (db/error @app-db) => #"test error"))

(facts "about `file/uploads-loaded`"
       (fact "sets uploads in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:file/uploads-loaded ..uploads..])
             (db/uploads @app-db) => ..uploads..))

(facts "about `file/upload`"
       (fact "sends request to backend"
             (let [e (catch-fx :http-xhrio)]
               (rf/dispatch-sync [:file/upload])
               @e => (just [(contains {:method :post
                                       :uri "api/uploads"})])))
       
       (fact "adds form data as body"
             (let [e (catch-fx :http-xhrio)]
               (rf/dispatch-sync [:file/upload])
               @e => (just [(contains {:body some?})])))

       (fact "clears errors"
             (simulate-fx :http-xhrio)
             (reset! app-db (db/set-error {} "test error")) => truthy
             (rf/dispatch-sync [:file/upload])
             (db/error @app-db) => nil?))

(facts "about `file/upload-succeeded`"
       (fact "sets last upload in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:file/upload-succeeded ..upload..])
             (db/last-upload @app-db) => ..upload..))

(facts "about `file/upload-failed`"
       (fact "sets error"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:file/upload-failed "test error"])
             (db/error @app-db) => #"test error"))

(facts "about `file/area-changed`"
       (fact "sets area in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:file/area-changed "journal"])
             (db/file-area @app-db) => "journal"))
