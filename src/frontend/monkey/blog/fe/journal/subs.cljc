(ns monkey.blog.fe.journal.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.journal.db :as db]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.utils :refer [db-fn]]))

(rf/reg-sub
 :journal/entries
 (db-fn db/journal-entries))

(def month-names ["january" "february" "march" "april" "may" "june"
                  "july" "august" "september" "october" "november" "december"])

(defn- parse-int [v]
  #?(:clj  (Integer/parseInt v)
     :cljs (js/parseInt v)))

(defn format-period
  ([p]
   (->> (or (some->> p
                     (re-matches #"^(\d{4})(\d{2})$")
                     (rest)
                     (map parse-int))
            [nil nil])
        (apply format-period)))
  ([y m]
   (-> (if (and y m)
         (t/make-date y m 1)
         (t/today))
       (t/format-month))))

(rf/reg-sub
 :journal/period-id
 (db-fn db/journal-period))

(rf/reg-sub
 :journal/period
 :<- [:journal/period-id]
 (fn [id _]
   (format-period id)))

;; Deprecated, replaced by ::month-counts
(rf/reg-sub
 :journal/months
 (fn [db _]
   (->> (db/journal-months db)
        (map (fn [[k v]] [(name k) (sort (map (fn [n]
                                                (if (seqable? n) (first n) n))
                                              v))]))
        (into {}))))

(rf/reg-sub
 :journal/month-counts
 (fn [db _]
   (->> (db/journal-months db)
        (map (fn [[k v]]
               (let [y (name k)]
                 [y {:months (sort-by first v)
                     :expanded? (db/journal-year-expanded? db y)}])))
        (into {}))))

(rf/reg-sub
 :journal/current
 (db-fn db/current-journal))

(rf/reg-sub
 :journal/filter-words
 (db-fn db/filter-words))

(rf/reg-sub
 :journal/search-results
 (db-fn db/search-results))
