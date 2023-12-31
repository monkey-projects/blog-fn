(ns monkey.blog.fe.journal.views
  "Journal related views"
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.journal.subs :as s]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.utils :as u]
            [monkey.blog.fe.tags :as tags]
            [monkey.blog.fe.routes :as r]))

(defn- entry [{:keys [id title contents created-on]}]
  [c/card
   title
   [:<>
    [:div.journal-time (t/format-date-time created-on)]
    (->> (tags/raw->html contents)
         (into [:div]))
    [:div.entry_time
     [:a {:href (r/path-for :journal/edit {:id id})}
      "edit"]]]])

(defn- entries []
  (rf/dispatch [:journal/load-entries])
  (fn []
    (let [e (rf/subscribe [:journal/entries])
          p (rf/subscribe [:journal/period])]
      [:div
       (cond
         (empty? @e)
         [:p "No entries found for " @p "."]
         (= 1 (count @e))
         [:p "Found one entry for " @p "."]
         :else
         [:p "Found " (count @e) " entries for " @p "."])
       (->> @e
            (map entry)
            (into [:<>]))])))

(defn- total-entries [m]
  (if (empty? m)
    0
    (reduce + (map second m))))

(defn- ->month-links [[y {:keys [months expanded?]}]]
  (->> (if expanded? months [])
       (map
        (fn [[m n]] [:a {:href (str "#/journal/" y (u/pad-zero (str m) 2))}
                     (get s/month-names (dec m)) [:span.count " (" n ")"]]))
       (into [:div
              [:a {:href ""
                   :class (if expanded? "expanded" "")
                   :on-click (u/evt-dispatch-handler [:journal/toggle-year y])}
               y [:span.count " (" (total-entries months) ")"]]])))
  
(defn- month-links []
  (let [m (rf/subscribe [:journal/month-counts])]
    (->> @m
         (sort-by first)
         (map ->month-links)
         (into [:div [:div.title "journal entries"]]))))

(defn- links []
  [:section
   [c/public-links]
   [c/private-links]
   [month-links]])

(defn- cancel-link []
  (let [p (rf/subscribe [:journal/period-id])]
    [:a {:href (str "#/journal/" @p)} "cancel"]))

(defn- edit-links [entry]
  (->> [[c/evt-link [:journal/save] "save"]
        (when (:id entry) [c/evt-link [:journal/delete] "delete"])
        [cancel-link]]
       (remove nil?)
       (interpose " | ")
       (into [:div])))

(defn edit []
  (let [e (rf/subscribe [:journal/current])]
    [:div.entry
     [:div.title "edit journal entry"]
     [:form
      [:p "date: " (:created-on @e)]
      [:div "body:" [:br]
       [:textarea {:value (:body @e)
                   :on-change (u/value-handler [:journal/changed])
                   :cols 55
                   :rows 20}]]
      [c/notification]
      [c/error]
      [edit-links @e]]]))

(defn- main-links []
  [c/link-para
   [:a {:href (r/path-for :journal/new)} "add a new entry"]
   [:a {:href (r/path-for :journal/search)} "search"]])

(defn view []
  (let [e (rf/subscribe [:journal/current])]
    [:<>
     [entry (assoc @e :title (str "view journal entry " (:id @e)))]
     [main-links]]))

(defn overview []
  (rf/dispatch [:journal/load-months])
  (fn []
    [:<>
     [:p "My journal.  If you are not me, then you've hacked this page. "
      "Congratulations, but you won't find a lot of interest here."]
     [entries]
     [main-links]]))
