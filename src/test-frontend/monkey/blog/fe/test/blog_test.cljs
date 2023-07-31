(ns monkey.blog.fe.test.blog-test
  (:require [cljs.test :refer-macros [deftest testing is] :refer [use-fixtures]]
            [monkey.blog.fe.alerts :as alerts]
            [monkey.blog.fe.blog :as sut]
            [monkey.blog.fe.test.fixtures :as tf]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(rf/clear-subscription-cache!)

(use-fixtures :each tf/reset-db)

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
    (rf/dispatch-sync [:blog/load-latest--loaded {:id :latest}])
    (is (= {:id :latest} (sut/latest @app-db)))))

(deftest load-latest--failed-evt
  (testing "sets error on failure"
    (rf/dispatch-sync [:blog/load-latest--failed {:status 500 :success false :body "test error"}])
    (is (= "test error" (alerts/error @app-db)))
    (is (nil? (sut/latest @app-db))))

  (testing "sets empty on 404 not found"
    (rf/dispatch-sync [:blog/load-latest--failed {:status 404 :success false}])
    (is (= {} (sut/latest @app-db)))))
