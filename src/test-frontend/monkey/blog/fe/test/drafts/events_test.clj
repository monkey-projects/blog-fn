(ns monkey.blog.fe.drafts.events-test
  (:require [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [monkey.blog.fe.helpers :refer :all]
            [monkey.blog.fe.drafts
             [db :as db]
             [events :as sut]]
            [monkey.blog.fe
             [alerts :as a]
             [db :as cdb]]))

(facts "about `drafts/load`"
       (fact "loads all from backend"
             (let [e (catch-fx :http-xhrio)]
               (rf/dispatch-sync [:drafts/load])
               @e => (just [(contains {:method :get
                                       :uri "api/drafts"})]))))

(facts "about `drafts/loaded`"
       (fact "stores drafts in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:drafts/loaded ..drafts..])
             (db/drafts @app-db) => ..drafts..))

(facts "about `draft/edit`"
       (fact "gets draft from local db"
             (reset! app-db (db/set-drafts {} [{:id 12 :title "test draft"}])) => truthy
             (rf/dispatch-sync [:draft/edit "12"])
             (db/current-draft @app-db) => (contains {:title "test draft"}))

       (fact "sets current panel to draft/edit"
             (reset! app-db (db/set-drafts {} [{:id 17 :title "test draft"}])) => truthy
             (rf/dispatch-sync [:draft/edit "17"])
             (cdb/current-panel @app-db) => (contains {:panel :draft/edit}))

       (future-fact "when not found locally, retrieves from backend"))

(facts "about `draft/publish`"
       (fact "sends request to backend"
             (let [e (catch-fx :http-xhrio)]
               (rf/dispatch-sync [:draft/publish {:id "test-draft"}])
               @e => (just [(contains {:method :post
                                       :uri "api/drafts/test-draft/publish"})])))

       (future-fact "clears notifications"))

(facts "about `draft/published`"
       (fact "sets notification"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/published ..draft.. {:id ..entry-id..}])
             (a/notification @app-db) => string?)

       (future-fact "marks draft as published"))

(facts "about `draft/publish-failed`"
       (fact "sets error"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/publish-failed ..error..])
             (a/error @app-db) => truthy))

(facts "about `draft/save`"
       (fact "saves new draft in backend"
             (let [e (catch-http)]
               (reset! app-db (db/set-current-draft {} {:title "title"
                                                        :body "test body"})) => truthy
               (rf/dispatch-sync [:draft/save])
               @e => (just [(contains {:method :post
                                       :uri "api/drafts"
                                       :params {:title "title"
                                                :body "test body"}})])))

       (fact "saves existing draft in backend"
             (let [e (catch-http)]
               (reset! app-db (db/set-current-draft {} {:id 12
                                                        :title "title"
                                                        :body "test body"})) => truthy
               (rf/dispatch-sync [:draft/save])
               @e => (just [(contains {:method :put
                                       :uri "api/drafts/12"
                                       :params {:id 12
                                                :title "title"
                                                :body "test body"}})]))))

(facts "about `draft/saved`"
       (fact "replaces current draft"
             (reset! app-db (db/set-current-draft {} ..draft..)) => map?
             (rf/dispatch-sync [:draft/saved ..updated..])
             (db/current-draft @app-db) => ..updated..)

       (fact "sets notification"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/saved ..updated..])
             (a/notification @app-db) => truthy)

       (fact "updates existing draft in list"
             (reset! app-db (db/set-drafts {} [{:id ..id..}])) => truthy
             (rf/dispatch-sync [:draft/saved {:id ..id.. :title "test draft"}])
             (db/drafts @app-db) => (just [{:id ..id.. :title "test draft"}]))

       (fact "adds new draft to list"
             (reset! app-db (db/set-drafts {} [{:id ..first..}])) => truthy
             (rf/dispatch-sync [:draft/saved {:id ..second.. :title "test draft"}])
             (db/drafts @app-db) => (just [{:id ..first..}
                                           {:id ..second.. :title "test draft"}])))

(facts "about `draft/save-failed`"
       (fact "sets error"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/save-failed ..error..])
             (a/error @app-db) => truthy))

(facts "about `draft/delete`"
       (fact "deletes in backend"
             (let [e (catch-http)]
               (rf/dispatch-sync [:draft/delete {:id 123}])
               @e => (just [(contains {:method :delete
                                       :uri "api/drafts/123"})]))))

(facts "about `draft/deleted`"
       (fact "sets notification"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/deleted ..draft..])
             (a/notification @app-db) => truthy)

       (fact "removes draft from list"
             (reset! app-db (db/set-drafts {} [{:id ..first..}
                                               {:id ..second..}])) => truthy
             (rf/dispatch-sync [:draft/deleted {:id ..second..}])
             (db/drafts @app-db) => (just [{:id ..first..}])))

(facts "about `draft/delete-failed`"
       (fact "sets error"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/delete-failed ..error..])
             (a/error @app-db) => truthy))

(facts "about `draft/changed`"
       (fact "updates draft property"
             (reset! app-db (db/set-current-draft {} {})) => map?
             (rf/dispatch-sync [:draft/changed :title "test title"])
             (db/current-draft @app-db) => (contains {:title "test title"})))

(facts "about `draft/new`"
       (fact "sets current draft"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/new])
             (db/current-draft @app-db) => map?)

       (fact "sets current panel"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [:draft/new])
             (cdb/current-panel @app-db) => (contains {:panel :draft/edit})))
