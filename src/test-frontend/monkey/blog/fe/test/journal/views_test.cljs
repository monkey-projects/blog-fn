(ns monkey.blog.fe.test.journal.views-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [monkey.blog.fe.journal.views :as sut]))

(deftest overview-links
  (testing "wraps links in a paragraph"
    (let [l (sut/overview-links)]
      (is (vector? l))
      (is (= :p (first l))))))
