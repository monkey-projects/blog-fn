(ns monkey.blog.fe.views
  "Main views"
  (:require [monkey.blog.fe.blog]
            [monkey.blog.fe.blog.views :as blog]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.login :as l]
            [monkey.blog.fe.panels :as p]
            [monkey.blog.fe.routing :as r]
            [re-frame.core :as rf]))

(defn home []
  [:<>
   [c/intro]
   [blog/latest-entry]])

(p/reg-panel ::r/root home)
