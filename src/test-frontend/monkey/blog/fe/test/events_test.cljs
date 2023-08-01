(ns monkey.blog.fe.test.events-test
  (:require [cljs.test :as t :refer-macros [deftest testing is]]
            [monkey.blog.fe.events :as sut]
            [monkey.blog.fe.test.fixtures :as tf]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(t/use-fixtures :each (tf/restore-re-frame) tf/reset-db)

(rf/clear-subscription-cache!)

(deftest initialize-db
  (testing "sets base path from location"
    (rf/reg-cofx :location (fn [cofx _]
                             (assoc cofx :location {:host "http://test"
                                                    :path "/base"})))
    (rf/dispatch-sync [:initialize-db])
    (is (= "/base"
           (:base-path @app-db))))

  (testing "drops `index.html`"
    (rf/reg-cofx :location (fn [cofx _]
                             (assoc cofx :location {:host "http://test"
                                                    :path "/base/index.html"})))
    (rf/dispatch-sync [:initialize-db])
    (is (= "/base"
           (:base-path @app-db))))

  (testing "initializes panels"
    (rf/dispatch-sync [:initialize-db])
    (is (some? (:monkey.blog.fe.panels/panels @app-db)))))
