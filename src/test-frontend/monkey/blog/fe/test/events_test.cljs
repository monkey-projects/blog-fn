(ns monkey.blog.fe.test.events-test
  (:require [cljs.test :as t :refer-macros [deftest testing is]]
            [monkey.blog.fe.events :as sut]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(defn- restore-re-frame []
  (let [restore-point (atom nil)]
    {:before #(reset! restore-point (rf/make-restore-fn))
     :after  #(@restore-point)}))

(t/use-fixtures :each [(restore-re-frame)])

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
           (:base-path @app-db)))))
