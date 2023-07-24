(ns monkey.blog.fe.core
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reitit.frontend.easy :as rfe]))

(defn main []
  [:div.content
   [:div.card
    [:div.title "Welcome"]
    [:p "Dynamic content loaded"]]])

(defn ^:dev/after-load reload! []
  #_(routing/start!)
  (let [root (js/document.getElementById "app")]
    (rdom/unmount-component-at-node root)
    (rdom/render [main] root)))

(defn ^:export init []
  (reload!))
