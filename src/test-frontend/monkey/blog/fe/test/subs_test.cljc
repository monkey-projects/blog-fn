(ns monkey.blog.fe.test.subs-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.subs :as s]))

(rf/clear-subscription-cache!)

(deftest login-credentials
  (let [e (rf/subscribe [:login/credentials])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns credentials from db"
      (is (some? (reset! app-db (db/set-credentials {} :creds))))
      (is (= :creds @e)))))

(deftest error
  (let [e (rf/subscribe [:error])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns error from db"
      (is (some? (reset! app-db (db/set-error {} :test-error))))
      (is (= :test-error @e)))))

(deftest authenticated?
  (let [e (rf/subscribe [:authenticated?])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns authentication status from db"
      (is (empty? (reset! app-db {})))
      (is (false? @e))
      (is (some? (reset! app-db (db/set-authenticated {} true))))
      (is (true? @e)))))
