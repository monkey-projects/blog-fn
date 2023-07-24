(ns monkey.blog.fe.core
  (:require [monkey.blog.fe.alerts]
            [monkey.blog.fe.components :as c]
            #_[monkey.blog.journal.views]
            [monkey.blog.fe.login]
            [monkey.blog.fe.routing :as routing]
            [monkey.blog.fe.subs]
            [monkey.blog.fe.panels]
            [monkey.blog.fe.views]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reitit.frontend.easy :as rfe]))

(defn main []
  (let [p (rf/subscribe [:panels/current])]
    [:<>
     [c/error]
     [c/notification]
     (if (nil? @p)
       [:p "No panel"]
       [@p])]))

(defn ^:dev/after-load reload! []
  (routing/start!)
  (let [root (js/document.getElementById "app")]
    (rdom/unmount-component-at-node root)
    (rdom/render [main] root)))

(defn ^:export init []
  (reload!))
