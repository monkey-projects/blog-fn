(ns monkey.blog.fe.test.events-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.test.fixtures :as tf]
            [monkey.blog.fe.test.helpers :as th]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.events :as e]))

(rf/clear-subscription-cache!)

(use-fixtures :each (tf/restore-re-frame) tf/reset-db)

(deftest initialize-db
  (testing "marks authenticated if cookie found"
    (rf/reg-cofx :cookie (fn [ctx v]
                           (assoc-in ctx [:cookie :bliki-session] "test-session")))
    (rf/dispatch-sync [::e/initialize-db])
    (is (true? (db/authenticated? @app-db)))))

(deftest route-selected
  (testing "changes current panel"
    (rf/dispatch-sync [:route/selected :test-route])
    (is (= :test-route (-> (db/current-panel @app-db) :panel))))

  (testing "stores any parameters"
    (rf/dispatch-sync [:route/selected :test-route :arg1 :arg2])
    (is (= [:arg1 :arg2] (-> (db/current-panel @app-db) :params)))))

(deftest journal-load-months
  (testing "sends request to backend"
    (let [e (th/catch-fx :http-xhrio)]
      (rf/dispatch-sync [:journal/load-months])
      (is (= {:method :get
              :uri "api/journal/months"}
             (-> @e (first) (select-keys [:method :uri])))))))

(deftest journal-months-loaded
  (testing "sets months in db"
    (rf/dispatch-sync [:journal/months-loaded :test-months])
    (is (= :test-months (db/journal-months @app-db)))))

(deftest journal-set-period
  (let [e (th/catch-fx :dispatch)]
    (rf/dispatch-sync [:journal/set-period :test-period])
    
    (testing "sets period in db"
      (is (= :test-period (db/journal-period @app-db))))

    (testing "sets selected panel to `:journal`"
      (is (= :journal (-> (db/current-panel @app-db) :panel))))

    (testing "starts loading entries"
      (is (= [[:journal/load-entries]] @e)))))

(deftest journal-load-entries
  (testing "sends request to backend for current period"
    (let [e (th/catch-fx :http-xhrio)]
      (is (some? (reset! app-db (db/set-journal-period {} "202005"))))
      (rf/dispatch-sync [:journal/load-entries])
      (is (= {:method :get
              :uri "api/journal/month/202005"}
             (-> @e (first) (select-keys [:method :uri]))))))
  
  (testing "sends request to backend when no period"
    (let [e (th/catch-fx :http-xhrio)]
      (rf/dispatch-sync [:journal/load-entries])
      (is (= {:method :get
              :uri "api/journal/month"}
             (-> @e (first) (select-keys [:method :uri])))))))

(deftest journal-entries-loaded
  (testing "sets entries in db"
    (rf/dispatch-sync [:journal/entries-loaded :test-entries])
    (is (= :test-entries (db/journal-entries @app-db)))))

(deftest journal-edit
  (testing "sets given journal entry as current"
    (let [entry {:id :test-id}]
      (is (some? (reset! app-db (db/set-journal-entries {} [entry]))))
      (rf/dispatch-sync [:journal/edit (:id entry)])
      (is (= entry (db/current-journal @app-db)))))

  (testing "sets current panel to `:journal/edit`"
    (is (some? (reset! app-db (db/set-journal-entries {} [{:id :test-id}]))))
    (rf/dispatch-sync [:journal/edit :test-id])
    (is (= :journal/edit (-> (db/current-panel @app-db) :panel)))))

(deftest journal-view
  (testing "if current entry is same, does not load from backend"
    (let [e (th/catch-fx :http-xhrio)]
      (is (some? (reset! app-db (db/set-current-journal {} {:id :test-id :body "test entry"}))))
      (rf/dispatch-sync [:journal/view :test-id])
      (is (empty? @e))))
  
  (testing "if current entry is different, clears it and loads from backend"
    (let [e (th/catch-fx :http-xhrio)
          id "1324"]
      (is (some? (reset! app-db (db/set-current-journal {} {:id :other-id :body "test entry"}))))
      (rf/dispatch-sync [:journal/view id])
      (is (= {:method :get
              :uri "api/journal/1324"} (-> @e (first) (select-keys [:method :uri]))))))

  (testing "sets current panel to `:journal/view`"
    (rf/dispatch-sync [:journal/view :test-id])
    (is (= :journal/view (-> (db/current-panel @app-db) :panel)))))

(deftest journal-entry-loaded
  (testing "sets current entry"
    (rf/dispatch-sync [:journal/entry-loaded :test-entry])
    (is (= :test-entry (db/current-journal @app-db)))))

(deftest journal-new
  (testing "sets new journal entry as current"
    (rf/dispatch-sync [:journal/new :test-id])
    (is (map? (db/current-journal @app-db))))

  (testing "sets current panel to `:journal/edit`"
    (rf/dispatch-sync [:journal/new :test-id])
    (is (= :journal/edit (-> (db/current-panel @app-db) :pandl)))))

(deftest journal-save
  (testing "saves new to backend using `:post`"
    (let [e (th/catch-fx :http-xhrio)
          curr {:body "test body"}]
      (is (some? (reset! app-db (db/set-current-journal {} curr))))
      (rf/dispatch-sync [:journal/save])
      (is (= 1 (count @e)))
      (is (contains? (first @e)
                     {:method :post
                :uri "api/journal"
                :params curr}))))
  
  (testing "saves existing to backend using `:put`"
    (let [e (th/catch-fx :http-xhrio)
          curr {:id 1234 :body "test body"}]
      (is (some? (reset! app-db (db/set-current-journal {} curr))))
      (rf/dispatch-sync [:journal/save])
      (is (= 1 (count @e)))
      (is (contains? (first @e) {:method :put
                                 :uri "api/journal/1234"}))
      (is (contains? (-> @e (first) :params) curr))))

  (testing "adds timezone to time"
    (let [e (th/catch-fx :http-xhrio)
          curr {:id 1234 :body "test body" :created-on "2021-05-10T11:37:00"}]
      (is (some? (reset! app-db (db/set-current-journal {} curr))))
      (rf/dispatch-sync [:journal/save])
      (is (= 1 (count @e)))
      (is (= "2021-05-10T11:37:00+02:00" (-> @e
                                             (first)
                                             :params
                                             :created-on)))))

  (testing "does not add timezone if no time"
    (let [e (th/catch-fx :http-xhrio)
          curr {:id 1234 :body "test body"}]
      (is (some? (reset! app-db (db/set-current-journal {} curr))))
      (rf/dispatch-sync [:journal/save])
      (is (nil? (-> @e
                    (first)
                    :params
                    :created-on)))))

  (testing "sets notification"
    (is (some? (reset! app-db (db/set-current-journal {} {:description "test"}))))
    (rf/dispatch-sync [:journal/save])
    (is (string? (a/notification @app-db))))

  (testing "clears error"
    (is (some? (reset! app-db (a/set-error {} "test error"))))
    (rf/dispatch-sync [:journal/save])
    (is (nil? (a/error @app-db)))))

(deftest journal-save-succeeded
  (testing "redirects to journal page for period"
    (let [e (th/catch-fx :goto)]
      (is (some? (reset! app-db (db/set-journal-period {} "202005"))))
      (rf/dispatch-sync [:journal/save-succeeded])
      (is (= ["#/journal"] @e))))

  (testing "updates entry in local db"
    (th/simulate-fx :goto)
    (is (some? (reset! app-db (db/set-journal-entries
                               {}
                               [{:id :first :body "first entry"}
                                {:id :second :body "second entry"}]))))
    (rf/dispatch-sync [:journal/save-succeeded {:id :second
                                                :body "updated entry"}])
    (is (= [{:id :first :body "first entry"}
            {:id :second :body "updated entry"}]
           (db/journal-entries @app-db))))

  (testing "clears notification"
    (is (some? (reset! app-db (a/set-notification {} "test notification"))))
    (rf/dispatch-sync [:journal/save-succeeded {}])
    (is (nil? (a/notification @app-db)))))

(deftest journal-save-failed
  (testing "reports errors"
    (rf/dispatch-sync [:journal/save-failed "test error"])
    (is (re-matches #"test error" (db/error @app-db))))

  (testing "clears notification"
    (is (some? (reset! app-db (a/set-notification {} "test notification"))))
    (rf/dispatch-sync [:journal/save-failed "test error"])
    (is (nil? (a/notification @app-db)))))

(deftest journal-changed
  (testing "updates body of current"
    (is (some? (reset! app-db (db/set-current-journal
                               {}
                               {:id :test-id
                                :body "test value"}))))
    (rf/dispatch-sync [:journal/changed "updated value"])
    (is (= "updated value" (:body (db/current-journal @app-db))))))

(deftest journal-delete
  (testing "sends delete request for current entry"
    (let [e (th/catch-fx :http-xhrio)]
      (is (some? (reset! app-db (db/set-current-journal {} {:id "test-id"}))))
      (rf/dispatch-sync [:journal/delete])
      (is (contains? (first @e)
                     {:method :delete
                     :uri "api/journal/test-id"})))))

(deftest journal-delete-succeeded
  (testing "redirects to overview"
    (let [e (th/catch-fx :goto)]
      (rf/dispatch-sync [:journal/delete-succeeded :test-id])
      (is (= ["#/journal"] @e))))

  (testing "sets notification"
    (rf/dispatch-sync [:journal/delete-succeeded :test-id])
    (is (string? (a/notification @app-db))))

  (testing "clears current entry"
    (is (some? (reset! app-db (db/set-current-journal {} :curr))))
    (rf/dispatch-sync [:journal/delete-succeeded :test-id])
    (is (nil? (db/current-journal @app-db))))

  (testing "removes entry by id"
    (is (some? (reset! app-db (db/set-journal-entries
                               {}
                               [{:id :first}
                                {:id :second}]))))
    (rf/dispatch-sync [:journal/delete-succeeded :first])
    (is (= [{:id :second}] (db/journal-entries @app-db)))))

(deftest journal-delete-failed
  (testing "sets error"
    (rf/dispatch-sync [:journal/delete-failed :some-id])
    (is (string? (a/error @app-db)))))

(deftest journal-toggle-year
  (testing "marks year as expanded"
    (rf/dispatch-sync [:journal/toggle-year 2020])
    (is (true? (db/journal-year-expanded? @app-db 2020)))
    (is (false? (db/journal-year-expanded? @app-db 2019)))))

(deftest journal-search-filter
  (testing "updates filter words"
    (rf/dispatch-sync [:journal/search-filter "test filter"])
    (is (= "test filter" (db/filter-words @app-db)))))

(deftest journal-search
  (testing "sends request to backend with filter words"
    (let [e (th/catch-fx :http-xhrio)]
      (is (some? (reset! app-db (db/set-filter-words {} "word-1 word-2"))))
      (rf/dispatch-sync [:journal/search])
      (is (contains? {:method :get
                              :uri "api/journal"
                              :params {:words ["word-1" "word-2"]}}
                     (first @e)))))

  (testing "clears errors"
    (th/simulate-fx :http-xhrio)
    (is (some? (reset! app-db (db/set-error {} "test error"))))
    (rf/dispatch-sync [:journal/search])
    (is (nil? (db/error @app-db)))))

(deftest journal-search-failed
  (testing "sets error"
    (rf/dispatch-sync [:journal/search-failed "test error"])
    (is (re-matches #"test error" (db/error @app-db)))))

(deftest journal-search-succeeded
  (testing "sets search results"
    (rf/dispatch-sync [:journal/search-succeeded :results])
    (is (= :results (db/search-results @app-db)))))
