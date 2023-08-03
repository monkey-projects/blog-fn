(ns monkey.blog.fe.test.drafts.subs-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [monkey.blog.fe.drafts.db :as db]
            [monkey.blog.fe.drafts.subs :as sut]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(deftest drafts
  (let [d (rf/subscribe [:drafts])]
    (testing "exists"
      (is (some? d)))
    
    (testing "retrieves drafts from db"
      (is (some? (reset! app-db (db/set-drafts {} :test-drafts))))
      (is (= :test-drafts @d)))))

(deftest draft-current
  (let [c (rf/subscribe [:draft/current])]
    (testing "exists"
      (is (some? c)))
    
    (testing "retrieves current draft from db"
      (is (some? (reset! app-db (db/set-current-draft {} :test-draft))))
      (is (= :test-draft @c)))))
