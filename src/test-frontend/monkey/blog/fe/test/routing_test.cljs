(ns monkey.blog.fe.test.routing-test
  (:require [cljs.test :as t :refer-macros [deftest is testing]]
            [day8.re-frame.test :as rf-test]
            [monkey.blog.fe.routing :as sut]
            [monkey.blog.fe.test.fixtures :as tf]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(t/use-fixtures :each (tf/restore-re-frame) tf/reset-db)

(deftest on-route-change
  (testing "dispatches `route/goto` event"
    (rf-test/run-test-sync
     (is (nil? (sut/on-route-change {:data {:name :test-match}} :test-history)))
     (is (= :test-match (-> (:route/current @app-db)
                            :data
                            :name))))))

(deftest current-sub
  (let [c (rf/subscribe [:route/current])]

    (testing "exists"
      (is (some? c)))

    (testing "returns current route from db"
      (is (empty? (reset! app-db {})))
      (is (nil? @c))
      (is (not-empty (reset! app-db {:route/current :test-route})))
      (is (= :test-route @c)))))

(deftest start-evt
  (let [router (atom nil)]
    (rf/reg-fx :routing/start (partial reset! router))
    
    (testing "invokes `routing/start` fx"
      (is (some? (reset! app-db {:base-path "/base"})))
      (rf/dispatch-sync [:routing/start])
      (is (some? @router)))
    
    (testing "adds router to db"
      (rf/dispatch-sync [:routing/start])
      (is (some? (sut/router @app-db))))))

(deftest path-for
  (testing "calculates path for route"
    ;; Need to start before we can call `path-for` to register history object
    (is (some? (sut/start! (sut/make-router nil))))
    (is (= "/journal" (sut/path-for ::sut/journal)))))
