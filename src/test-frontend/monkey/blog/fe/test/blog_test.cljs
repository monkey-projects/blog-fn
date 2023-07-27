(ns monkey.blog.fe.test.blog-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [monkey.blog.fe.blog :as sut]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(deftest latest-sub
  (let [l (rf/subscribe [:blog/latest])]

    (testing "exists"
      (is (some? l)))

    (testing "returns latest from db"
      (reset! app-db (sut/set-latest {} :test-entry))
      (is (= :test-entry @l)))))

(deftest load-latest-evt
  (testing "still todo"))

(deftest load-latest--loaded-evt
  (testing "sets latest blog entry in db"
    (is (empty? (reset! app-db {})))
    (rf/dispatch-sync [:blog/load-latest--loaded {:id :latest}])
    (is (= {:id :latest} (sut/latest @app-db)))))
