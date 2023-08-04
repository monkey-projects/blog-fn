(ns monkey.blog.fe.blog.views
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.tags :as tags]
            [monkey.blog.fe.time :as t]))

(defn- show-entry [{:keys [title body creation-date views]}]
  [:div.entry
   [:div.title title]
   [:div.journal_time (t/format-date-time creation-date) ", " views " views"]
   (->> (tags/raw->html body)
        (into [:div]))])

(defn- intro []
  [:p
   "Some of my rants on software development, the future of mankind and other small things. "
   "Never mind the occasional rape of the english language. "
   "I'm sure your dutch is much better than my english."])

(defn latest []
  (rf/dispatch [:blog/latest])
  (fn []
    (let [e (rf/subscribe [:blog/latest])]
      [:<>
       [intro]
       (if (nil? @e)
         [:div.entry "No blog entries found."]
         [show-entry @e])])))
