(ns monkey.blog.fe.journal.views
  (:require [monkey.blog.fe.components :as c]
            [monkey.blog.fe.journal.events :as e]
            [monkey.blog.fe.journal.subs :as s]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [monkey.blog.fe.utils :as u]
            [re-frame.core :as rf]))

(defn overview-links []
  (c/link-para
   [:a {:href (r/path-for ::r/journal--new)} "add a new entry"]
   [:a {:href (r/path-for ::r/journal--search)} "search"]))

(defn overview []
  [:<>
   [:p "These are my private parts, welcome.  But only if you're me.  Otherwise, get out!"]
   [overview-links]])

(defn edit-entry [])

(defn new-entry []
  [c/edit-entry {:input-sub [::s/current-entry]
                 :on-change-evt [::e/prop-changed]
                 :on-save-evt [::e/save]
                 :cancel-path (r/path-for ::r/journal)}])
