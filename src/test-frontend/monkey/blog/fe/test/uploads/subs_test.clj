(ns monkey.blog.fe.uploads.subs-test
  (:require [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [monkey.blog.fe.uploads.subs :as sut]
            [monkey.blog.fe.db :as db]
            [midje.sweet :refer :all]))

(rf/clear-subscription-cache!)

(facts "about `file/uploads`"
       (let [u (rf/subscribe [:file/uploads])]
         
         (fact "exists"
               u => some?)

         (fact "returns uploads from db"
               (reset! app-db (db/set-uploads {} ..uploads..)) => truthy
               @u => ..uploads..)))

(facts "about `file/last-upload`"
       (let [l (rf/subscribe [:file/last-upload])]

         (fact "exists"
               l => some?)

         (fact "returns last upload from db"
               (reset! app-db (db/set-last-upload {} ..last..)) => truthy
               @l => ..last..)))

(facts "about `file/area`"
       (let [a (rf/subscribe [:file/area])]

         (fact "exists"
               a => some?)

         (fact "returns file area"
               @a => nil?
               (reset! app-db (db/set-file-area {} "journal")) => truthy
               @a => "journal")))
