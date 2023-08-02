(ns monkey-blog.fe.subs-test
  (:require [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [monkey-blog.fe
             [db :as db]
             [subs :as s]
             [time :as t]]))

(rf/clear-subscription-cache!)

(facts "about `current-panel`"
       (let [c (rf/subscribe [:current-panel])]
         (fact "exists"
               c => some?)
       
         (fact "returns current panel from db"
               (reset! app-db (db/set-current-panel {} :test-panel []))
               @c => (contains {:panel :test-panel}))))

(facts "about `journal/entries`"
       (let [e (rf/subscribe [:journal/entries])]
         (fact "exists"
               e => some?)
         
         (fact "returns entries from db"
               (reset! app-db (db/set-journal-entries {} ..entries..)) => truthy
               @e => ..entries..)))

(facts "about `journal/period`"
       (let [e (rf/subscribe [:journal/period])]
         (fact "exists"
               e => some?)
         
         (fact "returns current journal period from db"
               (reset! app-db (db/set-journal-period {} "202005")) => truthy
               @e => "May 2020")))

(facts "about `journal/period-id`"
       (let [e (rf/subscribe [:journal/period-id])]
         (fact "exists"
               e => some?)
         
         (fact "returns current journal period id from db"
               (reset! app-db (db/set-journal-period {} "202005")) => truthy
               @e => "202005")))

(facts "about `journal/months`"
       (let [e (rf/subscribe [:journal/months])]
         (fact "exists"
               e => some?)
         
         (fact "returns months from db"
               (reset! app-db (db/set-journal-months {} {:2020 [[1 3] [2 1]]})) => truthy
               @e => {"2020" [1 2]})
         
         (fact "is backwards compatible"
               (reset! app-db (db/set-journal-months {} {:2020 [1 2]})) => truthy
               @e => {"2020" [1 2]})
         
         (fact "sorts months"
               (reset! app-db (db/set-journal-months {} {:2020 [[2 2] [1 3]]})) => truthy
               @e => {"2020" [1 2]})))

(facts "about `journal/month-counts`"
       (let [e (rf/subscribe [:journal/month-counts])]
         (fact "exists"
               e => some?)
         
         (fact "returns months and counts from db"
               (reset! app-db (db/set-journal-months {} {:2020 [[1 3] [2 1]]})) => truthy
               @e => (contains {"2020"
                                (contains {:months [[1 3] [2 1]]})}))
         
         (fact "sorts months"
               (reset! app-db (db/set-journal-months {} {:2020 [[2 2] [1 3]]})) => truthy
               @e => (contains {"2020"
                                (contains {:months [[1 3] [2 2]]})}))

         (fact "indicates expansion status"
               (reset! app-db (-> {}
                                  (db/set-journal-months {:2020 [[1 1]]})
                                  (db/toggle-journal-year "2020"))) => truthy
               @e => {"2020" {:months [[1 1]]
                              :expanded? true}})))

(facts "about `format-period`"
       (fact "nil as this month"
             (s/format-period nil) => ..month..
             (provided (t/today) => ..today..
                       (t/format-month ..today..) => ..month..))

       (fact "formats year/month as separate params"
             (s/format-period 2020 5) => "May 2020")

       (fact "formats year/month as period"
             (s/format-period "202005") => "May 2020"))

(facts "about `journal/current`"
       (let [e (rf/subscribe [:journal/current])]
         (fact "exists"
               e => some?)
         
         (fact "returns current journal entry from db"
               (reset! app-db (db/set-current-journal {} ..current..)) => truthy
               @e => ..current..)))

(facts "about `journal/filter-words`"
       (let [f (rf/subscribe [:journal/filter-words])]
         (fact "exists"
               f => some?)

         (fact "returns filter words from db"
               (reset! app-db (db/set-filter-words {} "test words")) => truthy
               @f => "test words")))

(facts "about `journal/search-results`"
       (let [r (rf/subscribe [:journal/search-results])]
         (fact "exists"
               r => some?)

         (fact "returns search results from db"
               (reset! app-db (db/set-search-results {} ..results..)) => truthy
               @r => ..results..)))

(facts "about `login/credentials`"
       (let [e (rf/subscribe [:login/credentials])]
         (fact "exists"
               e => some?)
         
         (fact "returns credentials from db"
               (reset! app-db (db/set-credentials {} ..creds..)) => truthy
               @e => ..creds..)))

(facts "about `error`"
       (let [e (rf/subscribe [:error])]
         (fact "exists"
               e => some?)
         
         (fact "returns error from db"
               (reset! app-db (db/set-error {} ..error..)) => truthy
               @e => ..error..)))

(facts "about `authenticated?`"
       (let [e (rf/subscribe [:authenticated?])]
         (fact "exists"
               e => some?)
         
         (fact "returns authentication status from db"
               @e => false?
               (reset! app-db (db/set-authenticated {} true)) => truthy
               @e => true?)))
