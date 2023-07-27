(ns monkey.blog.fe.views
  "Main views"
  (:require [monkey.blog.fe.blog :as blog]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.login :as l]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [re-frame.core :as rf]))

(defn latest-blog-entry []
  (let [l (rf/subscribe [:blog/latest])]
    (when (nil? @l)
      (rf/dispatch [:blog/load-latest])
      [:p "Loading latest blog entry..."])))

(defn home []
  [:<>
   [c/intro]
   [c/card
    "Welcome"
    [latest-blog-entry]]])

(p/reg-panel ::r/root home)
