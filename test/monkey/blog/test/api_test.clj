(ns monkey.blog.test.api-test
  (:require [clojure.test :refer :all]
            [monkey.blog
             [api :as sut]
             [persist :as p]]))

(def test-ctx {:monkey.blog/config {:storage (p/make-memory-storage)}
               :parameters {:path {:area "test"}}})

(defn- clean-db [f]
  (reset! (get-in test-ctx [:monkey.blog/config :storage :store]) nil)
  (f))

(use-fixtures :each clean-db)

(deftest list-entries
  (testing "empty when no entries"
    (is (empty? (:body (sut/list-entries test-ctx)))))
  
  (testing "lists all entries when no filter"
    (is (some? (sut/create-entry (assoc test-ctx :body {:title "test entry"
                                                        :contents "This is a test"}))))
    (is (= 1 (-> (sut/list-entries test-ctx)
                 :body
                 (count)))))
  
  (testing "lists entries with id filter"
    (let [id (-> test-ctx
                 (assoc-in [:parameters :body] {:title "test entry"
                                                :contents "This is a test"})
                 (sut/create-entry)
                 :body
                 :id)]
      (is (some? (sut/create-entry (assoc test-ctx :body {:title "another entry"}))))
      (let [matches (-> (sut/list-entries (assoc test-ctx :query-params {:id id}))
                        :body)]
        (is (= 1 (count matches)))
        (is (= "test entry" (:title (first matches))))))))

(deftest get-entry
  (testing "status `404` when entry not found"
    (is (= 404 (-> (sut/get-entry (assoc-in test-ctx [:parameters :path :id] (random-uuid)))
                   :status))))
  
  (testing "retrieves entry by id"
    (let [id (-> test-ctx
                 (assoc-in [:parameters :body] {:title "test entry"
                                                :contents "This is a test"})
                 (sut/create-entry)
                 :body
                 :id)]
      (is (some? id))
      (is (= "test entry" (-> (sut/get-entry (assoc-in test-ctx [:parameters :path :id] id))
                              :body
                              :title))))))
