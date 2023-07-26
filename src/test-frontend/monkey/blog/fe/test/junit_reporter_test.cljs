(ns monkey.blog.fe.test.junit-reporter-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [clojure.string :as cs]
            [monkey.blog.fe.test.junit-reporter :as sut]))

(deftest update-report
  (testing "begin-run-tests"
    (let [r (sut/update-report {:type :begin-run-tests
                                ::sut/state :test-state})]
      (testing "writes `<testsuites>` tag"
        (is (cs/includes? (::sut/out r) "<testsuites>")))
    
      (testing "returns state"
        (is (= :test-state (::sut/state r))))))

  (testing "end-run-tests"
    (let [r (sut/update-report {:type :end-run-tests})]
      (testing "writes `<testsuites>` end tag"
        (is (cs/includes? (::sut/out r) "</testsuites>")))))

  (testing "begin-test-ns"
    (let [r (sut/update-report {:type :begin-test-ns
                                :ns "test.ns"})]
      (testing "writes `<testsuite>` tag"
        (is (cs/includes? (::sut/out r) "<testsuite>")))))

  (testing "end-test-ns"
    (let [r (sut/update-report {:type :end-test-ns
                                :ns "test.ns"})]
      (testing "writes `<testsuite>` end tag"
        (is (cs/includes? (::sut/out r) "</testsuite>"))))))
