(ns monkey.blog.fe.subs
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.time :as t]))

(rf/reg-sub
 :journal/entries
 (fn [db _]
   (db/journal-entries db)))

(def month-names ["january" "february" "march" "april" "may" "june"
                  "july" "august" "september" "october" "november" "december"])

(defn- pad-zero [s]
  (cond->> s
    (= (count s) 1) (str "0")))

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
 (fn [db _]
   (db/journal-period db)))

(rf/reg-sub
 :journal/period
 (fn [_ _]
   (rf/subscribe [:journal/period-id]))
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
 (fn [db _]
   (db/current-journal db)))

(rf/reg-sub
 :journal/filter-words
 (fn [db _]
   (db/filter-words db)))

(rf/reg-sub
 :journal/search-results
 (fn [db _]
   (db/search-results db)))

(rf/reg-sub
 :login/credentials
 (fn [db _]
   (db/credentials db)))

(rf/reg-sub
 :error
 (fn [db _]
   (db/error db)))

(rf/reg-sub
 :authenticated?
 (fn [db _]
   (db/authenticated? db)))
