(ns monkey.blog.fe.test.blog.subs-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [monkey.blog.fe.blog.subs :as sut]
            [monkey.blog.fe.blog.db :as db]
            [monkey.blog.fe.time :as t]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(deftest blog-latest
  (let [l (rf/subscribe [:blog/latest])]
    (testing "exists"
      (is (some? l)))
    
    (testing "returns latest entry from db"
      (is (some? (reset! app-db (db/set-latest-entry {} {:id :test-latest}))))
      (is (= :test-latest (:id @l))))

    (testing "parses time"
      (is (some? (reset! app-db (db/set-latest-entry {} {:time "2023-08-28T11:00:00.000+02:00"}))))
      (is (t/datetime? (:time @l))))))
