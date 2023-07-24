(ns monkey.blog.fe.test.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [monkey.blog.fe.core :as sut]))

(deftest main
  (testing "returns vector"
    (is (vector? (sut/main)))))
