(ns monkey.blog.fe.test.time-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [monkey.blog.fe.time :as sut]))

(deftest today
  (testing "creates time object"
    (is (number? (.-year (sut/today))))))

(deftest parse-date-time
  (testing "parses from ISO date"
    (is (some? (sut/parse-date-time "2023-08-02"))))
  
  (testing "parses from ISO date/time"
    (is (some? (sut/parse-date-time "2023-08-02T13:30:00"))))

  (testing "leaves date/time object as-is"
    (let [d (sut/today)]
      (is (= d (sut/parse-date-time d))))))

(deftest make-date
  (testing "creates date object"
    (let [d (sut/make-date 2023 8 2)]
      (is (= 2023 (.-year d))))))

(deftest format-date-time
  (testing "formats full date time"
    (let [d (sut/make-date 2023 8 2)]
      (is (= "Wed, Aug 2, 2023, 12:00 AM" (sut/format-date-time d))))))

(deftest format-month
  (testing "formats month and year"
    (is (= "August 2023" (sut/format-month (sut/make-date 2023 8 1))))))

(deftest tz-offset
  (testing "returns timezone offset"
    (is (re-matches #"(\+|-)\d{2}:\d{2}" (sut/tz-offset)))))
