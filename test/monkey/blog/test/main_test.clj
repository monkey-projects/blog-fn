(ns monkey.blog.test.main-test
  (:require [clojure.test :refer :all]
            [monkey.blog
             [main :as sut]
             [persist :as p]]
            [org.httpkit.server :as http]))

(def storage (p/make-memory-storage))
(def test-handler (sut/make-handler {:storage storage}))

(deftest handler
  (testing "is a fn"
    (is (fn? sut/handler)))

  (testing "GET /health"
    (is (= 200 (-> {:request-method :get
                    :uri "/health"}
                   (test-handler)
                   :status))))

  (testing "`GET /api/entries/:id`"
    (testing "returns 404 when not found"
      (is (= 404 (-> {:request-method :get
                      :uri (str "/api/entries/blog/" (random-uuid))}
                     (test-handler)
                     :status))))
    
    (testing "retrieves by id"
      (let [id (p/write-entry storage {:title "test"})]
        (is (= 200 (-> {:request-method :get
                        :uri (str "/api/entries/blog/" id)}
                       (test-handler)
                       :status)))))))

(deftest -main
  (testing "starts http server"
    (with-redefs-fn {#'http/run-server (constantly :test-server)}
      #(is (= :test-server (sut/-main))))))
