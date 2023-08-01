(ns monkey.blog.fe.views
  "Main views"
  (:require [monkey.blog.fe.blog]
            [monkey.blog.fe.blog.views :as blog]
            [monkey.blog.fe.components :as c]
            [monkey.blog.fe.login :as l]
            [re-frame.core :as rf]))

(defn home []
  [:<>
   [c/intro]
   [blog/latest-entry]])


