(ns monkey.blog.fe.comps
  "Shared components"
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.routes :as r]
            [monkey.blog.fe.utils :as u]))

(defn public-links
  "Links available on the public site"
  []
  [:<>
   [:section
    [:div.title "stuff I like"]
    [:a {:href "http://www.joelonsoftware.com"} "joel on software"]
    [:a {:href "https://archlinux.org/"} "arch linux"]
    [:a {:href "https://www.givewell.org/"} "givewell"]
    [:a {:href "https://ourworldindata.org/"} "our world in data"]
    [:a {:href "https://clojure.org"} "clojure"]]
   [:section
    [:div.title "stuff I'm working on"]
    [:a {:href "http://monkeyci.com"} "monkey ci"]]])

(defn private-links
  "Links only available in the secure area"
  []
  [:section
   [:div.title "navigation"]
   [:a {:href (r/path-for :root)} "weblog"]
   #_[:a {:href "/admin"} "admin"]
   [:a {:href (r/path-for :journal)} "journal"]
   [:a {:href (r/path-for :drafts)} "drafts"]
   [:a {:href (r/path-for :upload)} "upload files"]])

(defn links
  "Shows public links, and if the user has been authenticated, also the private links"
  []
  (let [a (rf/subscribe [:authenticated?])]
    [:div.links
     [public-links]
     (when @a
       [private-links])]))

(defn evt-link [evt lbl]
  [:a {:href ""
       :on-click (u/evt-dispatch-handler evt)} lbl])

(defn error
  "Renders error component"
  []
  (let [e (rf/subscribe [:error])]
    (when @e
      [:div.error @e])))

(defn notification
  "Renders notification component"
  []
  (let [e (rf/subscribe [:alerts/notification])]
    (when @e
      [:div.notification @e])))

(defn ^:deprecated with-layout
  "Wraps given `p` component in the default layout, with the specified links to show"
  [links p]
  [:table {:width "100%" :cell-padding 0}
   [:tbody
    [:tr
     [:td {:valign "top" :width "100%"}
      p]
     [:td {:valign "top" :align "right" :nowrap "true"}
      [:div.links links]]]]])

(defn card [title & contents]
  (into [:div.card
         (when title
           [:div.title title])]
        contents))

(defn link-para
  "Renders a paragraph with multiple links"
  [& links]
  (->> links
       (interpose " | ")
       (vec)
       (into [:p])))
