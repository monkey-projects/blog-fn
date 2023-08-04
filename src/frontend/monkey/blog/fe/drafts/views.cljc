(ns monkey.blog.fe.drafts.views
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.tags :as tags]
            [monkey.blog.fe.utils :as u]))

(defn- action-links [{:keys [id] :as d}]
  [:div.actions
   [:a {:href (str "#/drafts/edit/" id)} "edit"]
   " | "
   [:a {:href "" :on-click (u/evt-dispatch-handler [:draft/delete d])} "delete"]
   " | "
   [:a {:href "" :on-click (u/evt-dispatch-handler [:draft/publish d])} "publish"]])

(defn- draft-summary [d]
  [:div.entry
   [:div.journal_time (t/format-date-time (:last-updated d))]
   [:p [:b (:title d)]]
   (->> (tags/raw->html (:body d))
        (into [:div]))
   [action-links d]])

(defn- drafts [d]
  (->> (mapv draft-summary d)
       (into [:div])))

(defn draft-overview []
  (let [d (rf/subscribe [:drafts])]
    (when (nil? @d)
      (rf/dispatch [:drafts/load]))
    [:div.content
     [:div.entry
      [:div.title "Drafts"]
      (if (nil? @d)
        [:p "Loading drafts..."]
        [:p "Found " (count @d) " draft(s)"])]
     [:p
      [c/error]
      [c/notification]]
     [drafts @d]
     [:p
      [:a {:href "#/drafts/new"} "new"]]]))

(defn- cancel-link []
  [:a {:href "#/drafts"} "cancel"])

(defn- edit-links [d]
  (->> [[c/evt-link [:draft/save] "save"]
        (when (:id d) [c/evt-link [:draft/delete] "delete"])
        [cancel-link]]
       (remove nil?)
       (interpose " | ")
       (into [:div])))

(defn edit-draft []
  (let [draft (rf/subscribe [:draft/current])]
    [:div.entry
     [:div.title "edit draft"]
     [:form
      [:div "title:" [:br]
       [:input {:type :text
                :value (:title @draft)
                :on-change (u/value-handler [:draft/changed :title])}]]
      [:div "body:" [:br]
       [:textarea {:value (:body @draft)
                   :on-change (u/value-handler [:draft/changed :body])
                   :cols 55
                   :rows 20}]]]
     [c/notification]
     [c/error]
     [edit-links @draft]]))
