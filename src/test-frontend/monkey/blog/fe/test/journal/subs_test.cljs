(ns monkey.blog.fe.test.journal.subs-test
  (:require [cljs.test :refer-macros [deftest testing is] :as t]
            [monkey.blog.fe.journal.db :as db]
            [monkey.blog.fe.journal.subs :as sut]
            [monkey.blog.fe.test.fixtures :as tf]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(t/use-fixtures :each tf/reset-db)

(deftest current-entry
  (let [c (rf/subscribe [::sut/current-entry])]
    
    (testing "exists"
      (is (some? c)))

    (testing "returns current entry from db"
      (is (nil? @c))
      (reset! app-db (db/set-current {} :test-entry))
      (is (= :test-entry @c)))))
