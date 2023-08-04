(ns monkey.blog.fe.search
  "Views for searching"
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.journal.views :as j]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.utils :as u]))

(defn- render-matches [m]
  (->> m
       (map (fn [{:keys [word indexes]}]
              [:span.match word ": " (count indexes) " match" (when (not= 1 (count indexes)) "es")]))
       (interpose ", ")
       (into [:div.matches])))

(defn- ->result-entry [{:keys [entry matches]}]
  [:div.entry
   [:div.journal_time (t/format-date-time (:created-on entry))]
   [:p (str (subs (:body entry) 0 60) "...")]
   [render-matches matches]
   [:a.entry_time {:href (str "#/journal/view/" (:id entry))} "view"]])

(defn- results-list [matches]
  (->> matches
       (map ->result-entry)
       (into [:div.results-list])))

(defn search-results []
  (let [r (rf/subscribe [:journal/search-results])]
    (when @r
      [:div.search-results
       [:p "Found " (count @r) " matching result(s)"]
       [results-list @r]])))

(defn search-form []
  (let [w (rf/subscribe [:journal/filter-words])]
    [:div.content
     [:div.entry
      [:div.title" search journal entries"]
      [:form
       [:label {:for "filter"} "search for:"]
       [:br]
       [:input {:type :text
                :size 50
                :id "filter"
                :value @w
                :on-change (u/value-handler [:journal/search-filter])}]
       [:p
        [:a {:href ""
             :on-click (u/evt-dispatch-handler [:journal/search])} "search"]
        " | "
        [:a {:href "#/journal"} "cancel"]]]
      [c/error]]
     [search-results]]))
