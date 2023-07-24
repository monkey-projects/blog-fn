(ns monkey.blog.fe.components
  (:require [monkey.blog.fe.utils :as u]
            [re-frame.core :as rf]))

(defn intro []
  [:p
   "Some of my rants on software development, the future of mankind and other small things. "
   "Never mind the occasional rape of the english language. I'm sure your dutch is much "
   "better than my english."])

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
   [:a {:href "/"} "weblog"]
   [:a {:href "/admin"} "admin"]
   [:a {:href "/journal"} "journal"]
   [:a {:href "/drafts"} "drafts"]
   [:a {:href "/upload"} "upload files"]])

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
  ([]
   (error @(rf/subscribe [:alerts/error])))
  ([{:keys [message retry] :as err}]
   (when err
     [:div.error
      message
      (when retry
        [:span.retry [evt-link retry "retry"]])])))

(defn notification []
  (let [e (rf/subscribe [:alerts/notification])]
    (when @e
      [:div.notification @e])))