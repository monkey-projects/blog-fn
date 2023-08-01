(ns monkey.blog.fe.test.journal.events-test
  (:require [cljs.test :refer-macros [deftest testing is] :as t]
            [monkey.blog.fe.journal.db :as db]
            [monkey.blog.fe.journal.events :as sut]
            [monkey.blog.fe.test.fixtures :as tf]
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(t/use-fixtures :each tf/reset-db)

(deftest prop-changed
  (testing "updates property in current entry"
    (is (empty? (db/current @app-db)))
    (rf/dispatch-sync [::sut/prop-changed :contents "updated contents"])
    (is (= {:contents "updated contents"} (db/current @app-db)))))
