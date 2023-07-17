(ns monkey.blog.test.api-test
  (:require [clojure.test :refer :all]
            [monkey.blog
             [api :as sut]
             [persist :as p]]))

(def test-ctx {:storage (p/make-memory-storage)
               :area "test"})

(defn- clean-db [f]
  (reset! (get-in test-ctx [:storage :store]) nil)
  (f))

(use-fixtures :each clean-db)

(deftest list-entries
  (testing "empty when no entries"
    (is (empty? (sut/list-entries test-ctx nil))))
  
  (testing "lists all entries when no filter"
    (is (some? (sut/create-entry test-ctx {:title "test entry"
                                           :contents "This is a test"})))
    (is (= 1 (count (sut/list-entries test-ctx nil)))))
  
  (testing "lists entries with id filter"
    (let [id (sut/create-entry test-ctx {:title "test entry"
                                         :contents "This is a test"})]
      (is (some? (sut/create-entry test-ctx {:title "another entry"})))
      (let [matches (sut/list-entries test-ctx {:id id})]
        (is (= 1 (count matches)))
        (is (= "test entry" (:title (first matches))))))))
