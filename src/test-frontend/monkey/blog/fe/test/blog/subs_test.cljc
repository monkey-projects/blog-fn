(ns monkey.blog.fe.test.blog.subs-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [monkey.blog.fe.blog.subs :as sut]
            [monkey.blog.fe.blog.db :as db]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(deftest blog-latest
  (let [l (rf/subscribe [:blog/latest])]
    (testing "exists"
      (is (some? l)))
    
    (testing "returns latest entry from db"
      (is (some? (reset! app-db (db/set-latest-entry {} :test-latest))))
      (is (= :test-latest @l)))))
