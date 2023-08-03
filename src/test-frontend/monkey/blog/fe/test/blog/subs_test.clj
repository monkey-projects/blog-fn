(ns monkey.blog.fe.blog.subs-test
  (:require [monkey.blog.fe.blog
             [subs :as sut]
             [db :as db]]
            [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(rf/clear-subscription-cache!)

(facts "about `blog/latest`"
       (let [l (rf/subscribe [:blog/latest])]
         (fact "exists"
               l => some?)

         (fact "returns latest entry from db"
               (reset! app-db (db/set-latest-entry {} ..latest..)) => truthy
               @l => ..latest..)))
