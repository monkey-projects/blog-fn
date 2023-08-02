(ns monkey.blog.fe.test.events-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.test.helpers :refer :all]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.events :as e]))

(rf/clear-subscription-cache!)

(deftest initialize-db
  (testing "marks authenticated if cookie found"
    (let [f (rf/make-restore-fn)]
      (rf/reg-cofx :cookie (fn [ctx v]
                             (assoc-in ctx [:cookie :bliki-session] "test-session")))
      (rf/dispatch-sync [::e/initialize-db])
      (db/authenticated? @app-db) => true?
      (f) => irrelevant)))

(deftest route-selected
  (testing "changes current panel"
    (rf/dispatch-sync [:route/selected :test-route]) => nil?
    (db/current-panel @app-db) => (contains {:panel :test-route}))

  (testing "stores any parameters"
    (rf/dispatch-sync [:route/selected :test-route ..arg1.. ..arg2..]) => nil?
    (db/current-panel @app-db) => {:panel :test-route
                                   :params [..arg1.. ..arg2..]}))

(deftest journal-load-months
  (testing "sends request to backend"
    (let [e (catch-fx :http-xhrio)]
      (rf/dispatch-sync [:journal/load-months]) => nil?
      @e => (just
             [(contains {:method :get
                         :uri "api/journal/months"})]))))

(deftest journal-months-loaded
  (testing "sets months in db"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/months-loaded ..months..]) => nil?
    (db/journal-months @app-db) => ..months..))

(deftest journal-load-months-failed
  (future-testing "reports errors"))

(deftest journal-set-period
  (testing "sets period in db"
    (reset! app-db {})
    (rf/dispatch-sync [:journal/set-period ..period..])
    (db/journal-period @app-db) => ..period..)

  (testing "sets selected panel to `:journal`"
    (reset! app-db {})
    (rf/dispatch-sync [:journal/set-period ..period..])
    (db/current-panel @app-db) => (contains {:panel :journal}))

  (testing "starts loading entries"
    (let [e (catch-fx :dispatch)]
      (rf/dispatch-sync [:journal/set-period ..period..])
      @e => (just [[:journal/load-entries]]))))

(deftest journal-load-entries
  (testing "sends request to backend for current period"
    (let [e (catch-fx :http-xhrio)]
      (reset! app-db (db/set-journal-period {} "202005")) => truthy
      (rf/dispatch-sync [:journal/load-entries]) => nil?
      @e => (just
             [(contains {:method :get
                         :uri "api/journal/month/202005"})])))
  
  (testing "sends request to backend when no period"
    (let [e (catch-fx :http-xhrio)]
      (reset! app-db {}) => map?
      (rf/dispatch-sync [:journal/load-entries]) => nil?
      @e => (just
             [(contains {:method :get
                         :uri "api/journal/month"})]))))

(deftest journal-entries-loaded
  (testing "sets entries in db"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/entries-loaded ..entries..]) => nil?
    (db/journal-entries @app-db) => ..entries..))

(deftest journal-load-entries-failed
  (future-testing "reports errors"))

(deftest journal-edit
  (testing "sets given journal entry as current"
    (let [entry {:id ..id..}]
      (reset! app-db (db/set-journal-entries {} [entry])) => truthy
      (rf/dispatch-sync [:journal/edit (:id entry)])
      (db/current-journal @app-db) => entry))

  (testing "sets current panel to `:journal/edit`"
    (reset! app-db (db/set-journal-entries {} [{:id ..id..}])) => truthy
    (rf/dispatch-sync [:journal/edit ..id..])
    (db/current-panel @app-db) => (contains {:panel :journal/edit})))

(deftest journal-new
  (testing "if current entry is same, does not load from backend"
    (let [e (catch-fx :http-xhrio)]
      (reset! app-db (db/set-current-journal {} {:id ..id.. :body "test entry"})) => truthy
      (rf/dispatch-sync [:journal/view ..id..])
      @e => empty?))
  
  (testing "if current entry is different, clears it and loads from backend"
    (let [e (catch-fx :http-xhrio)
          id 1324]
      (reset! app-db (db/set-current-journal {} {:id ..other-id.. :body "test entry"})) => truthy
      (rf/dispatch-sync [:journal/view id])
      @e => (just [(contains {:method :get
                              :uri "api/journal/1324"})])))

  (testing "sets current panel to `:journal/view`"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/view ..id..])
    (db/current-panel @app-db) => (contains {:panel :journal/view})))

(deftest journal-entry-loaded
  (testing "sets current entry"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/entry-loaded ..test-entry..])
    (db/current-journal @app-db) => ..test-entry..))

(deftest journal-new
  (testing "sets new journal entry as current"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/new ..id..])
    (db/current-journal @app-db) => map?)

  (testing "sets current panel to `:journal/edit`"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/new ..id..])
    (db/current-panel @app-db) => (contains {:panel :journal/edit})))

(deftest journal-save
  (testing "saves new to backend using `:post`"
    (let [e (catch-fx :http-xhrio)
          curr {:body "test body"}]
      (reset! app-db (db/set-current-journal {} curr)) => truthy
      (rf/dispatch-sync [:journal/save]) => nil?
      (count @e) => 1
      @e => (just
             [(contains
               {:method :post
                :uri "api/journal"
                :params (contains curr)})])))
  
  (testing "saves existing to backend using `:put`"
    (let [e (catch-fx :http-xhrio)
          curr {:id 1234 :body "test body"}]
      (reset! app-db (db/set-current-journal {} curr)) => truthy
      (rf/dispatch-sync [:journal/save]) => nil?
      (count @e) => 1
      @e => (just
             [(contains
               {:method :put
                :uri "api/journal/1234"
                :params (contains curr)})])))

  (testing "adds timezone to time"
    (let [e (catch-fx :http-xhrio)
          curr {:id 1234 :body "test body" :created-on "2021-05-10T11:37:00"}]
      (reset! app-db (db/set-current-journal {} curr)) => truthy
      (rf/dispatch-sync [:journal/save]) => nil?
      (count @e) => 1
      (-> @e
          (first)
          :params
          :created-on)) => "2021-05-10T11:37:00+02:00")

  (testing "does not add timezone if no time"
    (let [e (catch-fx :http-xhrio)
          curr {:id 1234 :body "test body"}]
      (reset! app-db (db/set-current-journal {} curr)) => truthy
      (rf/dispatch-sync [:journal/save]) => nil?
      (-> @e
          (first)
          :params
          :created-on)) => nil?)

  (testing "sets notification"
    (reset! app-db (db/set-current-journal {} {:description "test"})) => truthy
    (rf/dispatch-sync [:journal/save]) => nil?
    (a/notification @app-db) => string?)

  (testing "clears error"
    (reset! app-db (a/set-error {} "test error")) => truthy
    (rf/dispatch-sync [:journal/save]) => nil?
    (a/error @app-db) => nil?))

(deftest journal-save-succeeded
  (testing "redirects to journal page for period"
    (let [e (catch-fx :goto)]
      (reset! app-db (db/set-journal-period {} "202005")) => truthy
      (rf/dispatch-sync [:journal/save-succeeded])
      @e => (just ["#/journal"])))

  (testing "updates entry in local db"
    (simulate-fx :goto)
    (reset! app-db (db/set-journal-entries {} [{:id ..first.. :body "first entry"}
                                               {:id ..second.. :body "second entry"}])) => truthy
    (rf/dispatch-sync [:journal/save-succeeded {:id ..second..
                                                :body "updated entry"}])
    (db/journal-entries @app-db) => (just [{:id ..first.. :body "first entry"}
                                           {:id ..second.. :body "updated entry"}]
                                          :in-any-order))

  (testing "clears notification"
    (reset! app-db (a/set-notification {} "test notification")) => truthy
    (rf/dispatch-sync [:journal/save-succeeded {}])
    (a/notification @app-db) => nil?))

(deftest journal-save-failed
  (testing "reports errors"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/save-failed "test error"])
    (db/error @app-db) => #"test error")

  (testing "clears notification"
    (reset! app-db (a/set-notification {} "test notification")) => truthy
    (rf/dispatch-sync [:journal/save-failed "test error"])
    (a/notification @app-db) => nil?))

(deftest journal-changed
  (testing "updates body of current"
    (reset! app-db (db/set-current-journal {}
                                           {:id ..id..
                                            :body "test value"})) => truthy
    (rf/dispatch-sync [:journal/changed "updated value"])
    (:body (db/current-journal @app-db)) => "updated value"))

(deftest journal-delete
  (testing "sends delete request for current entry"
    (let [e (catch-fx :http-xhrio)]
      (reset! app-db (db/set-current-journal {} {:id "test-id"})) => truthy
      (rf/dispatch-sync [:journal/delete])
      @e => (just [(contains
                    {:method :delete
                     :uri "api/journal/test-id"})]))))

(deftest journal-delete-succeeded
  (testing "redirects to overview"
    (let [e (catch-fx :goto)]
      (rf/dispatch-sync [:journal/delete-succeeded :test-id])
      @e => (just ["#/journal"])))

  (testing "sets notification"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/delete-succeeded :test-id])
    (a/notification @app-db) => string?)

  (testing "clears current entry"
    (reset! app-db (db/set-current-journal {} ..curr..)) => truthy
    (rf/dispatch-sync [:journal/delete-succeeded :test-id])
    (db/current-journal @app-db) => nil?)

  (testing "removes entry by id"
    (reset! app-db (db/set-journal-entries {}
                                           [{:id ..first..}
                                            {:id ..second..}])) => truthy
    (rf/dispatch-sync [:journal/delete-succeeded ..first..])
    (db/journal-entries @app-db) => (just [{:id ..second..}])))

(deftest journal-delete-failed
  (testing "sets error"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/delete-failed ..id..])
    (a/error @app-db) => string?))

(deftest journal-toggle-year
  (testing "marks year as expanded"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/toggle-year 2020])
    (db/journal-year-expanded? @app-db 2020) => true?
    (db/journal-year-expanded? @app-db 2019) => false?))

(deftest journal-search-filter
  (testing "updates filter words"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/search-filter "test filter"]) => irrelevant
    (db/filter-words @app-db) => "test filter"))

(deftest journal-search
  (testing "sends request to backend with filter words"
    (let [e (catch-fx :http-xhrio)]
      (reset! app-db (db/set-filter-words {} "word-1 word-2")) => truthy
      (rf/dispatch-sync [:journal/search])
      @e => (just [(contains {:method :get
                              :uri "api/journal"
                              :params {:words ["word-1" "word-2"]}})])))

  (testing "clears errors"
    (simulate-fx :http-xhrio)
    (reset! app-db (db/set-error {} "test error")) => truthy
    (rf/dispatch-sync [:journal/search])
    (db/error @app-db) => nil?))

(deftest journal-search-failed
  (testing "sets error"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/search-failed "test error"])
    (db/error @app-db) => #"test error"))

(deftest journal-save-succeeded
  (testing "sets search results"
    (reset! app-db {}) => truthy
    (rf/dispatch-sync [:journal/search-succeeded ..results..])
    (db/search-results @app-db) => ..results..))
