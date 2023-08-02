(ns monkey.blog.fe.core
  (:require [cljsjs.react]
            [reagent.core :as reagent]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.cookies :as cookies]
            [monkey.blog.fe.config :as config]
            [monkey.blog.fe.routes :as routes]
            [monkey.blog.fe.events :as e]
            [monkey.blog.fe.subs]
            [monkey.blog.fe.blog.events]
            [monkey.blog.fe.blog.subs]
            [monkey.blog.fe.login.events]
            [monkey.blog.fe.drafts.events]
            [monkey.blog.fe.drafts.subs]
            [monkey.blog.fe.uploads.events]
            [monkey.blog.fe.uploads.subs]
            [monkey.blog.fe.views :as v]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load reload! []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [v/main-panel] root-el)))

(defn init []
  (try
    (rf/dispatch-sync [::e/initialize-db])
    (rf/dispatch-sync [:routing/start])
    (dev-setup)
    (let [loc js/location]
      (martian/init (str (.-origin loc) "/swagger.json")))
    (reload!)
    (catch js/Object ex
      (println "Failed to initialize:" ex))))
