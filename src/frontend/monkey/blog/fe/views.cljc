(ns monkey.blog.fe.views
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.journal.views :as journal]
            [monkey.blog.fe.login.views :as login]
            [monkey.blog.fe.routes :as r]
            [monkey.blog.fe.search :as search]
            [monkey.blog.fe.uploads.views :as uploads]
            [monkey.blog.fe.blog.views :as blog]
            [monkey.blog.fe.drafts.views :as drafts]))

(defn- current-panel [route-id params]
  (case route-id
    :journal         (->> (:path params)
                          (into [journal/overview]))
    :journal/edit    [journal/edit]
    :journal/view    [journal/view]
    :journal/search  [search/search-form]
    :uploads         [uploads/new-upload]
    :uploads/list    [uploads/list-uploads]
    :drafts          [drafts/draft-overview]
    :draft/edit      [drafts/edit-draft]
    ;; Default
    [blog/latest]))

(defn main-panel []
  (let [auth? (rf/subscribe [:authenticated?])
        route (rf/subscribe [:route/current])
        public? #(or (nil? %) (#{:root} %))
        route-id (get-in @route [:data :name])]
    (println "Current route:" route-id)
    [:<>
     [:div.content
      [c/error]
      [c/notification]
      (if-not (or @auth? (public? route-id))
        ;; User wants access to secure area, show login panel
        [login/login-panel]
        ;; User has either been authenticated, or the area is public
        [current-panel route-id (get-in @route [:data :parameters])])]
     [c/links]]))
