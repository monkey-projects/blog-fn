(ns monkey.blog.fe.journal.views
  (:require [monkey.blog.fe.components :as c]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [re-frame.core :as rf]))

(defn overview []
  [:<>
   [:p "These are my private parts, welcome.  But only if you're me.  Otherwise, get out!"]])

