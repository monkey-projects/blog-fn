(ns monkey.blog.fe.uploads.views
  "Views for managing uploads"
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.utils :as u]))

(defn- links []
  [:div
   [c/public-links]
   [c/private-links]])

(defn- upload-row [{:keys [id name content-type area url]}]
  [:tr
   [:td id]
   [:td [:a {:href url :target "_blank" :title name}
         (subs name 0 50)]]
   [:td area]])

(defn- uploads-list [u]
  (->> u
       (mapv upload-row)
       (into [[:tbody]])
       (into [:table
              [:thead
               [:tr
                [:th "id"]
                [:th "name"]
                [:th "area"]]]])))
              
(defn list-uploads []
  (rf/dispatch [:file/list-uploads])
  (fn []
    (let [u (rf/subscribe [:file/uploads])]
      [:div.content
       [:div.entry
        [:div.title "uploaded files"]
        (if @u
          [:div
           [:p (str "Found " (count @u) " uploaded files")]
           [uploads-list @u]]
          [:p "Retrieving uploaded files..."])]])))

(defn- upload-form []
  (let [a (rf/subscribe [:file/area])]
    [:form
     [:div
      [:div.form-group
       [:label {:for :file} "file to upload:"]
       [:input {:type :file
                :id :file}]]
      [:div.form-group
       [:label {:for :visibility} "file visibility:"]
       [:select {:id :visibility
                 :value @a
                 :on-change (u/value-handler [:file/area-changed])}
        [:option "public"]
        [:option "journal"]]]]
     [:div.actions
      [:a {:href ""
           :on-click (u/evt-dispatch-handler [:file/upload])} "upload"]
      " | "
      [:a {:href "#/"} "cancel"]]]))

(defn last-upload []
  (let [l (rf/subscribe [:file/last-upload])]
    (when @l
      [:p "File uploaded with id " [:a {:href (:url @l)
                                        :target "_blank"}
                                    (:id @l)]])))

(defn new-upload []
  [:div.content
   [:div.entry
    [:div.title "upload file"]
    [upload-form]
    [last-upload]]
   [:p
    [:a {:href "#/upload/list"} "view uploaded files"]]])
