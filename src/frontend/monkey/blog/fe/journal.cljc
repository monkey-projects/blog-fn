(ns monkey.blog.fe.journal
  "Journal related views"
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.subs :as s]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.utils :as u]
            [monkey.blog.fe.tags :as tags]))

(defn- entry [{:keys [id body created-on]}]
  [:div.entry
   [:div.journal_time (t/format-date-time created-on)]
   (->> (tags/raw->html body)
        (into [:div]))
   [:div.entry_time
    [:a {:href (str "#/journal/edit/" id)}
     "edit"]]])

(defn- entries []
  (rf/dispatch [:journal/load-entries])
  (fn []
    (let [e (rf/subscribe [:journal/entries])
          p (rf/subscribe [:journal/period])]
      [:div
       [c/notification]
       (cond
         (empty? @e)
         [:p "No entries found for " @p "."]
         (= 1 (count @e))
         [:p "Found one entry for " @p "."]
         :else
         [:p "Found " (count @e) " entries for " @p "."])
       (->> @e
            (map entry)
            (into [:div]))])))

(defn- pad-zero [s]
  (cond->> s
    (= (count s) 1) (str "0")))

(defn- total-entries [m]
  (if (empty? m)
    0
    (reduce + (map second m))))

(defn- ->month-links [[y {:keys [months expanded?]}]]
  (->> (if expanded? months [])
       (map
        (fn [[m n]] [:a {:href (str "#/journal/" y (pad-zero (str m)))}
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

(defn with-layout
  "Wraps given `p` component in the layout for journals"
  [p]
  (c/with-layout [links] p))

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
    (with-layout
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
        [edit-links @e]]])))

(defn- main-links []
  [:p
   [:a {:href "#/journal/new"} "add a new entry"]
   " | "
   [:a {:href "#/journal/search"} "search"]])

(defn view []
  (let [e (rf/subscribe [:journal/current])]
    (with-layout
      [:div.content
       [:div.entry
        [:div.title (str "view journal entry " (:id @e))]]
       [entry @e]
       [main-links]])))

(defn- contents []
  [:div.content
   [:p "My journal.  If you are not me, then you've hacked this page. "
    "Congratulations, but you won't find a lot of interest here."]
   [entries]
   [main-links]])

(defn overview []
  (rf/dispatch [:journal/load-months])
  (fn []
    (with-layout [contents])))
