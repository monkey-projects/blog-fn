(ns monkey.blog.fe.core
  (:require [martian.re-frame :as martian]
            [monkey.blog.fe.alerts]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.events]
            [monkey.blog.fe.location]
            [monkey.blog.fe.login]
            [monkey.blog.fe.routing :as routing]
            [monkey.blog.fe.subs]
            [monkey.blog.fe.panels]
            [monkey.blog.fe.views :as v]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reitit.frontend.easy :as rfe]))

(defn welcome-user
  "Displays a welcome message if the user is authenticated"
  []
  (let [u (rf/subscribe [:user])
        uname (some-fn :display-name :username :email)]
    (when @u
      [:p "Welcome, " (uname @u)])))

(defn main []
  (let [p (rf/subscribe [:panels/current])]
    [:<>
     [c/error]
     [c/notification]
     [:div.content
      [welcome-user]
      (if (nil? @p)
        [v/home]
        [@p])]
     [c/links]]))

(defn ^:dev/after-load reload! []
  (rf/dispatch-sync [:routing/start])
  (let [root (js/document.getElementById "app")]
    (rdom/unmount-component-at-node root)
    (rdom/render [main] root)))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (let [loc js/location]
    (martian/init (str (.-origin loc) "/swagger.json")))
  (reload!))
