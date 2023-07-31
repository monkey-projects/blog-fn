(ns monkey.blog.fe.test.fixtures
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

(defn restore-re-frame []
  (let [restore-point (atom nil)]
    {:before #(reset! restore-point (rf/make-restore-fn))
     :after  #(@restore-point)}))

(def reset-db {:before #(reset! app-db {})})
