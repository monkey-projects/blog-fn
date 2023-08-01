(ns monkey.blog.fe.blog.views
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.routing :as r]))

(defn load-latest []
  (rf/dispatch [:blog/load-latest])
  [c/card
   "Welcome"
   [:p "Loading latest blog entry..."]])

(defn show-no-entries []
  [c/card
   "No Entries"
   [:p "There appear to be no blog entries yet.  Perhaps you could "
    [:a {:href (r/path-for ::r/blog--new)} "write one"] "?"]])

(defn show-latest [{:keys [title contents]}]
  [c/card
   title
   [:p contents]])

(defn latest-entry
  "Displays latest blog entry"
  []
  (let [l (rf/subscribe [:blog/latest])]
    (cond
      (nil? @l) [load-latest]
      (empty? @l) [show-no-entries]
      :else [show-latest @l])))

(defn new-entry []
  [:p "Create new entry here"])
