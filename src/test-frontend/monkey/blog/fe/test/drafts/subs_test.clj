(ns monkey-blog.fe.drafts.subs-test
  (:require [monkey-blog.fe.drafts
             [db :as db]
             [subs :as sut]]
            [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(rf/clear-subscription-cache!)

(facts "about `:drafts`"
      (let [d (rf/subscribe [:drafts])]
        (fact "exists"
              d => some?)
        
        (fact "retrieves drafts from db"
              (reset! app-db (db/set-drafts {} ..drafts..)) => truthy
              @d => ..drafts..)))

(facts "about `draft/current`"
       (let [c (rf/subscribe [:draft/current])]
         (fact "exists"
               c => some?)
         
         (fact "retrieves current draft from db"
               (reset! app-db (db/set-current-draft {} ..draft..)) => map?
               @c => ..draft..)))
