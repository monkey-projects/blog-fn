(ns monkey.blog.test.main-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [monkey.blog
             [main :as sut]
             [persist :as p]]
            [org.httpkit.server :as http]
            [ring.mock.request :as mock]))

(def storage (p/make-memory-storage))
(def test-handler (sut/make-handler {:storage storage}))

(defn- status= [expected r]
  (= expected (:status r)))

(def not-found?  (partial status= 404))
(def success?    (partial status= 200))
(def created?    (partial status= 201))
(def no-content? (partial status= 204))

(defn json-> [in]
  (-> (io/reader in)
      (json/read :key-fn keyword)))

(defn- clean-db [f]
  (reset! (:store storage) nil)
  (f))

(use-fixtures :each clean-db)

(deftest handler
  (testing "is a fn"
    (is (fn? sut/handler)))

  (testing "GET /health"
    (is (success? (-> (mock/request :get "/health")
                      (test-handler)))))

  (testing "404 on unknown path"
    (is (not-found? (-> (mock/request :get "/unknown")
                        (test-handler)))))

  (testing "/api/entries"
    (testing "GET /:id"
      (testing "returns 404 when not found"
        (is (not-found? (-> (mock/request :get (str "/api/entries/blog/" (random-uuid)))
                            (test-handler)))))
      
      (testing "retrieves by id"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})
              response (-> (mock/request :get (str "/api/entries/blog/" id))
                           (test-handler))]
          (is (success? response))
          (is (= "test" (-> response
                            :body
                            (json->)
                            :title)))))
      
      (testing "not found if wrong area"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})]
          (is (not-found? (-> (mock/request :get (str "/api/entries/other/" id))
                              (test-handler)))))))

    (testing "GET /"
      (let [area "test"]
        (testing "404 if no entries"
          (is (not-found? (-> (mock/request :get (str "/api/entries/" area))
                              (test-handler)))))

        (testing "returns entries for area"
          (is (some? (p/write-entry storage {:title "test entry"
                                             :area area})))
          (is (success? (-> (mock/request :get (str "/api/entries/" area))
                            (test-handler)))))))

    (testing "POST /"
      (let [r (-> (mock/request :post "/api/entries/blog")
                  (mock/json-body {:title "test"
                                   :contents "Test item"})
                  (test-handler))]
        (testing "creates new entry"
          (is (created? r)))

        (testing "assigns id"
          (is (some? (-> r
                         :body
                         (json->)
                         :id))))))

    (testing "PUT /:id"
      (testing "updates existing entry"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})
              r (-> (mock/request :put (str "/api/entries/blog/" id))
                    (mock/json-body {:title "test"
                                     :contents "Test item"})
                    (test-handler))]
          (is (success? r)))))

    (testing "DELETE /:id"
      (let [area "test-delete-area"]
        (testing "deletes by id"
          (let [id (p/write-entry storage {:title "To delete"
                                           :area area})]
            (is (some? id))
            (is (no-content? (-> (mock/request :delete (str "/api/entries/" area "/" id))
                                 (test-handler))))))))))

(deftest -main
  (testing "starts http server"
    (with-redefs-fn {#'http/run-server (constantly :test-server)}
      #(is (= :test-server (sut/-main))))))
