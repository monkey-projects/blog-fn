(ns monkey.blog.fe.test.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [monkey.blog.fe.core :as sut]))

(deftest main
  (testing "returns vector"
    (is (vector? (sut/main)))))

#_(deftest failing-test
  (testing "just to see what happens when it fails"
    (is (= 0 1))))
