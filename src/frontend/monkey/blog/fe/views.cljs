(ns monkey.blog.fe.views
  "Main views"
  (:require [monkey.blog.fe.components :as c]
            [monkey.blog.fe.login :as l]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [re-frame.core :as rf]))

(defn home []
  (let [u (atom nil) #_(rf/subscribe [:firebase/user])]
    [:<>
     [:div.content
      (when @u
        [:p "Welcome, " (or (:display-name @u) (:email @u))])
      [c/intro]
      [:div.card
       [:div.title "Welcome"]
       [:p "Dynamic content loaded."]]]
     [c/links]]))

(p/reg-panel ::r/root home)
