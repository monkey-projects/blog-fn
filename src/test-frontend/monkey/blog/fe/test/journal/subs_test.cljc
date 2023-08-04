(ns monkey.blog.fe.test.journal.subs-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.journal.db :as db]
            [monkey.blog.fe.journal.subs :as s]
            [monkey.blog.fe.time :as t]))

(rf/clear-subscription-cache!)

(deftest journal-entries
  (let [e (rf/subscribe [:journal/entries])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns entries from db"
      (is (some? (reset! app-db (db/set-journal-entries {} :test-entries))))
      (is (= :test-entries @e)))))

(deftest journal-period
  (let [e (rf/subscribe [:journal/period])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns current journal period from db"
      (is (some? (reset! app-db (db/set-journal-period {} "202005"))))
      (is (= "May 2020" @e)))))

(deftest journal-period-id
  (let [e (rf/subscribe [:journal/period-id])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns current journal period id from db"
      (is (some? (reset! app-db (db/set-journal-period {} "202005"))))
      (is (= "202005" @e)))))

(deftest journal-months
  (let [e (rf/subscribe [:journal/months])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns months from db"
      (is (some? (reset! app-db (db/set-journal-months {} {:2020 [[1 3] [2 1]]}))))
      (is (= {"2020" [1 2]} @e)))
    
    (testing "is backwards compatible"
      (is (some? (reset! app-db (db/set-journal-months {} {:2020 [1 2]}))))
      (is (= {"2020" [1 2]} @e)))
    
    (testing "sorts months"
      (is (some? (reset! app-db (db/set-journal-months {} {:2020 [[2 2] [1 3]]}))))
      (is (= {"2020" [1 2]} @e)))))

(deftest journal-month-counts
  (let [e (rf/subscribe [:journal/month-counts])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns months and counts from db"
      (is (some? (reset! app-db (db/set-journal-months {} {:2020 [[1 3] [2 1]]}))))
      (is (= [[1 3] [2 1]] (get-in @e ["2020" :months]))))
    
    (testing "sorts months"
      (is (some? (reset! app-db (db/set-journal-months {} {:2020 [[2 2] [1 3]]}))))
      (is (= [[1 3] [2 2]] (get-in @e ["2020" :months]))))

    (testing "indicates expansion status"
      (is (some? (reset! app-db (-> {}
                                    (db/set-journal-months {:2020 [[1 1]]})
                                    (db/toggle-journal-year "2020")))))
      (is (= {"2020" {:months [[1 1]]
                      :expanded? true}}
             @e)))))

(deftest format-period
  (testing "nil as this month"
    (is (string? (s/format-period nil))))

  (testing "formats year/month as separate params"
    (is (= "May 2020" (s/format-period 2020 5))))

  (testing "formats year/month as period"
    (is (= "May 2020" (s/format-period "202005")))))

(deftest journal-current
  (let [e (rf/subscribe [:journal/current])]
    (testing "exists"
      (is (some? e)))
    
    (testing "returns current journal entry from db"
      (is (some? (reset! app-db (db/set-current-journal {} :current))))
      (is (= :current @e)))))

(deftest journal-filter-words
  (let [f (rf/subscribe [:journal/filter-words])]
    (testing "exists"
      (is (some? f)))

    (testing "returns filter words from db"
      (is (some? (reset! app-db (db/set-filter-words {} "test words"))))
      (is (= "test words" @f)))))

(deftest journal-search-results
  (let [r (rf/subscribe [:journal/search-results])]
    (testing "exists"
      (is (some? r)))

    (testing "returns search results from db"
      (is (some? (reset! app-db (db/set-search-results {} :results))))
      (is (= :results @r)))))
