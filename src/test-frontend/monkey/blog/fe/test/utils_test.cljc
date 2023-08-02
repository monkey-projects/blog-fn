(ns monkey.blog.fe.test.utils-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is]]])
            [monkey.blog.fe.utils :as sut]))

(deftest pad-left
  (testing "pads empty string"
    (is (= "xxx" (sut/pad-left "" "x" 3))))

  (testing "pads chars to left"
    (is (= "xa" (sut/pad-left "a" "x" 2)))))

(deftest pad-zero
  (testing "left pads with zeroes"
    (is (= "0002" (sut/pad-zero 2 4)))))
