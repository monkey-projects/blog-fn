(ns monkey.blog.fe.views
  "Main views"
  (:require [monkey.blog.fe.blog]
            [monkey.blog.fe.blog.views :as blog]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.journal.views]
            [monkey.blog.fe.login :as l]
            [re-frame.core :as rf]))

(defn home []
  [:<>
   [c/intro]
   [blog/latest-entry]])

(defn edit-entry
  "Displays form to edit an entry"
  [entry]
  [c/card
   "Edit Entry"
   [:form
    [:p "date:" [:input {:type :date}]]
    [:p "contents:"]
    [:textarea {:rows 20
                :cols 50}]]])
