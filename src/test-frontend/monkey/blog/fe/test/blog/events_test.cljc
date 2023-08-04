(ns monkey.blog.fe.test.blog.events-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.db :refer [app-db]]
            [re-frame.core :as rf]            
            [monkey.blog.fe.blog.db :as db]
            [monkey.blog.fe.blog.events :as e]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.test.helpers :as h]
            [monkey.blog.fe.test.fixtures :as tf]))

(use-fixtures :each tf/reset-db)

(deftest blog-latest
  (testing "loads latest from backend"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:blog/latest])
      (is (= :get-latest (ffirst @e)))
      (is (= "blog" (-> @e (first) (second) :area)))))

  (testing "sets notification"
    (rf/dispatch-sync [:blog/latest])
    (is (string? (a/notification @app-db)))))

(deftest latest-received
  (testing "sets entry in db"
    (rf/dispatch-sync [::e/latest-received {:body :test-latest}])
    (is (= :test-latest (db/latest-entry @app-db))))

  (testing "clears notification"
    (is (some? (reset! app-db (a/set-notification {} "test notification"))))
    (rf/dispatch-sync [::e/latest-received :test-latest])
    (is (nil? (a/notification @app-db)))))

(deftest latest-failed
  (testing "sets error"
    (rf/dispatch-sync [::e/latest-failed {:body "test error"}])
    (is (re-matches #".*test error.*" (db/error @app-db))))

  (testing "clears notification"
    (is (some? (reset! app-db (a/set-notification {} "test notification"))))
    (rf/dispatch-sync [::e/latest-failed "test error"])
    (is (nil? (a/notification @app-db)))))
