(ns monkey.blog.fe.login.events-test
  (:require [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [monkey.blog.fe.helpers :refer :all]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.login.events :as sut]))

(facts "about `login/username`"
       (fact "sets username in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:login/username "test-username"]) => nil?
             (db/credentials @app-db) => (contains {:username "test-username"})))

(facts "about `login/password`"
       (fact "sets password in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:login/password "test-password"]) => nil?
             (db/credentials @app-db) => (contains {:password "test-password"})))

(facts "about `login`"
       (fact "sends credentials to backend using basic auth"
             (let [e (catch-fx :http-xhrio)]
               (reset! app-db (db/set-credentials {} ..creds..)) => truthy
               (rf/dispatch-sync [:login]) => nil?
               @e => (just
                      [(contains {:method :post
                                  :uri "api/auth"
                                  :headers (contains {"Authorization" string?})})]))))

(facts "about `login/succeeded`"
       (fact "does not change current panel"
             (reset! app-db (db/set-current-panel {} :test-panel [])) => truthy
             (rf/dispatch-sync [:login/succeeded "OK"]) => irrelevant
             (db/current-panel @app-db) => (contains {:panel :test-panel}))

       (fact "sets authenticated in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:login/succeeded "OK"]) => irrelevant
             (db/authenticated? @app-db) => true?))

(facts "about `login/failed`"
       (fact "reports errors"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:login/failed]) => irrelevant
             (db/error @app-db) => string?))

(facts "about `login/logoff`"
       (fact "sends request to backend" 
             (let [e (catch-fx :http-xhrio)]
               (rf/dispatch-sync [:login/logoff]) => irrelevant
               @e => (just [(contains {:method :post
                                       :uri "api/auth/logoff"})]))))

(facts "about `logoff/success`"
       (let [r (catch-fx :goto)]
         (fact "redirects to home page"
               (rf/dispatch-sync [:logoff/success "ok"]) => irrelevant
               @r => (just ["#/"]))

         (fact "resets errors"
               (reset! app-db (db/set-error {} "test error")) => truthy
               (rf/dispatch-sync [:login/logoff]) => irrelevant
               (db/error @app-db) => nil?)))

(facts "about `logoff/failed`"
       (fact "reports errors"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:logoff/failed]) => irrelevant
             (db/error @app-db) => string?))
