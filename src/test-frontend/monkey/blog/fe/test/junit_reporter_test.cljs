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
        (is (cs/includes? (::sut/out r) "<testsuite>")))

      (testing "sets ns in state"
        (is (= "test.ns" (-> r ::sut/state :ns))))))

  (testing "end-test-ns"
    (let [r (sut/update-report {:type :end-test-ns
                                :ns "test.ns"
                                ::sut/state {:ns "test.ns"
                                             :vars [:a :b]}})]
      (testing "writes `<testsuite>` end tag"
        (is (cs/includes? (::sut/out r) "</testsuite>")))

      (testing "clears ns from state"
        (is (nil? (-> r ::sut/state :ns))))))

  (testing "begin-test-var"
    (let [r (sut/update-report {:type :begin-test-var
                                :var "test var"})]
      (testing "sets var in state"
        (is (= "test var" (-> r ::sut/state :var))))))

  (testing "end-test-var"
    (let [r (sut/update-report {:type :end-test-var
                                :var "test var"
                                ::sut/state {:var "test var"}})]
      (testing "clears var from state"
        (is (nil? (-> r ::sut/state :var))))))

  (testing "pass"
    (testing "sets pass in state"
      (is (= 1 (-> (sut/update-report {:type :pass})
                   ::sut/state
                   :pass))))
      
    (testing "increases pass in state"
      (is (= 2 (-> (sut/update-report {:type :pass
                                       ::sut/state {:pass 1}})
                   ::sut/state
                   :pass)))))

  (testing "fail"
    (testing "sets fail in state"
      (is (= 1 (-> (sut/update-report {:type :fail})
                   ::sut/state
                   :fail))))
      
    (testing "increases fail in state"
      (is (= 2 (-> (sut/update-report {:type :fail
                                       ::sut/state {:fail 1}})
                   ::sut/state
                   :fail)))))

  (testing "error"
    (testing "sets error in state"
      (is (= 1 (-> (sut/update-report {:type :error})
                   ::sut/state
                   :error))))
      
    (testing "increases error in state"
      (is (= 2 (-> (sut/update-report {:type :error
                                       ::sut/state {:error 1}})
                   ::sut/state
                   :error))))))
