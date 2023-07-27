(ns monkey.blog.fe.test.subs-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.subs :as sut]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(deftest panel-current
  (let [r (rf/subscribe [:panel/current])]

    (testing "exists"
      (is (some? r)))

    (testing "returns current panel from db"
      (is (map? (reset! app-db (db/set-current-panel {} :test-current))))
      (is (= :test-current @r)))))

(deftest user
  (let [u (rf/subscribe [:user])]

    (testing "exists"
      (is (some? u)))

    (testing "returns always nil for now"
      (is (nil? @u)))))

(deftest authenticated?
  (let [a (rf/subscribe [:authenticated?])]

    (testing "exists"
      (is (some? a)))

    (testing "always false for now"
      (is (false? @a)))))
