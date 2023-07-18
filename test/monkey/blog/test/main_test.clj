(ns monkey.blog.test.main-test
  (:require [clojure.test :refer :all]
            [monkey.blog.main :as sut]
            [org.httpkit.server :as http]))

(deftest handler
  (testing "is a fn"
    (is (fn? sut/handler)))

  (testing "GET /health"
    (is (= 200 (-> (sut/handler {:request-method :get
                                 :uri "/health"})
                   :status)))))

(deftest -main
  (testing "starts http server"
    (with-redefs-fn {#'http/run-server (constantly :test-server)}
      #(is (= :test-server (sut/-main))))))
