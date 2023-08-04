(ns monkey.blog.fe.test.events-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.test.fixtures :as tf]
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
