(ns monkey.blog.test.main-test
  (:require [clojure.test :refer :all]
            [monkey.blog
             [main :as sut]
             [persist :as p]]
            [org.httpkit.server :as http]
            [ring.mock.request :as mock]))

(def storage (p/make-memory-storage))
(def test-handler (sut/make-handler {:storage storage}))

(defn- status= [expected r]
  (= expected (:status r)))

(def not-found? (partial status= 404))
(def success?   (partial status= 200))
(def created?   (partial status= 201))

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
                                         :area "blog"})]
          (is (success? (-> (mock/request :get (str "/api/entries/blog/" id))
                            (test-handler))))))
      
      (testing "not found if wrong area"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})]
          (is (not-found? (-> (mock/request :get (str "/api/entries/other/" id))
                              (test-handler)))))))

    (testing "POST /"
      (testing "creates new entry"
        (let [r (-> (mock/request :post "/api/entries/blog")
                    (mock/json-body {:title "test"})
                    (test-handler))]
          (is (created? r)))))))

(deftest -main
  (testing "starts http server"
    (with-redefs-fn {#'http/run-server (constantly :test-server)}
      #(is (= :test-server (sut/-main))))))
