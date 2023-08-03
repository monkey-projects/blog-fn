(ns monkey.blog.fe.drafts.db-test
  (:require [monkey.blog.fe.drafts.db :as sut]
            [midje.sweet :refer :all]))

(facts "about `draft-by-id`"
       (fact "finds match by id"
             (-> {}
                 (sut/set-drafts [{:id ..first..}
                                  {:id ..second..}])
                 (sut/draft-by-id ..second..)) => {:id ..second..}))
