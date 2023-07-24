(ns monkey.blog.test.api-test
  (:require [clojure.test :refer :all]
            [monkey.blog
             [api :as sut]
             [persist :as p]]))

(def storage (p/make-memory-storage))
(def test-ctx {:monkey.blog/config {:storage storage}
               :parameters {:path {:area "test"}}})

(defn- clean-db [f]
  (reset! (:store storage) nil)
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
        (is (= "test entry" (:title (first matches))))
        (is (= id (:id (first matches))))))))

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

(deftest delete-entry
  (let [area "delete area"]
    
    (testing "removes entry by id from db"
      (let [id (p/write-entry storage {:title "to delete"
                                       :area area})]
        (is (= 204 (-> test-ctx
                       (assoc-in [:parameters :path] {:id id
                                                      :area area})
                       (sut/delete-entry)
                       :status)))
        (is (nil? (p/read-entry storage id)))))

    (testing "not found if entry does not exist"
        (is (= 404 (-> test-ctx
                       (assoc-in [:parameters :path] {:id "nonexisting-id"
                                                      :area area})
                       (sut/delete-entry)
                       :status))))))

(deftest update-entry
  (let [area "update area"]

    (testing "updates existing item"
      (let [id (p/write-entry storage {:title "to update"
                                       :area area})]
        (is (= 200 (-> test-ctx
                       (assoc :parameters
                              {:path {:id id
                                      :area area}
                               :body {:title "updated title"}})
                       (sut/update-entry)
                       :status)))
        (is (= "updated title" (-> (p/read-entry storage id)
                                   :title)))))

    (testing "404 if entry does not exist"
      (is (= 404 (-> test-ctx
                     (assoc :parameters
                            {:path {:id (str (random-uuid))
                                    :area area}
                             :body {:title "updated title"}})
                     (sut/update-entry)
                     :status))))

    (testing "404 if item is in different area"
      (let [id (p/write-entry storage {:title "original title"
                                       :area area})]
        (is (= 404 (-> test-ctx
                       (assoc :parameters
                              {:path {:id id
                                      :area "other-area"}
                               :body {:title "updated title"}})
                       (sut/update-entry)
                       :status)))
        (is (= "original title" (-> (p/read-entry storage id)
                                    :title)))))))
