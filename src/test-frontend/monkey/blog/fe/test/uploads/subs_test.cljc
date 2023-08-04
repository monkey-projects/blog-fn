(ns monkey.blog.fe.test.uploads.subs-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.uploads.subs :as sut]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.test.fixtures :as tf]))

(use-fixtures :each tf/reset-db)

(rf/clear-subscription-cache!)

(deftest file-uploads
  (let [u (rf/subscribe [:file/uploads])]
    
    (testing "exists"
      (is (some? u)))

    (testing "returns uploads from db"
      (is (some? (reset! app-db (db/set-uploads {} :test-uploads))))
      (is (= :test-uploads @u)))))

(deftest file-last-upload
  (let [l (rf/subscribe [:file/last-upload])]

    (testing "exists"
      (is (some? l)))

    (testing "returns last upload from db"
      (is (some? (reset! app-db (db/set-last-upload {} :test-upload))))
      (is (= :test-upload @l)))))

(deftest file-area
  (let [a (rf/subscribe [:file/area])]

    (testing "exists"
      (is (some? a)))

    (testing "returns file area"
      (is (nil? @a))
      (is (some? (reset! app-db (db/set-file-area {} "journal"))))
      (is (= "journal" @a)))))
