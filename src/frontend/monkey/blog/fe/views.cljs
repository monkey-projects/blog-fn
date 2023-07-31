(ns monkey.blog.fe.views
  "Main views"
  (:require [monkey.blog.fe.blog :as blog]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.login :as l]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [re-frame.core :as rf]))

(defn load-latest []
  (rf/dispatch [:blog/load-latest])
  [c/card
   "Welcome"
   [:p "Loading latest blog entry..."]])

(defn show-latest [{:keys [title contents]}]
  [c/card
   title
   [:p contents]])

(defn latest-blog-entry []
  (let [l (rf/subscribe [:blog/latest])]
    (if (nil? @l)
      [load-latest]
      [show-latest @l])))

(defn home []
  [:<>
   [c/intro]
   [latest-blog-entry]])

(p/reg-panel ::r/root home)
