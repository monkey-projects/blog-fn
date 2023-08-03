(ns monkey.blog.fe.test.drafts.db-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [monkey.blog.fe.drafts.db :as sut]))

(deftest draft-by-id
  (testing "finds match by id"
    (is (= {:id :second} (-> {}
                             (sut/set-drafts [{:id :first}
                                              {:id :second}])
                             (sut/draft-by-id :second))))))
