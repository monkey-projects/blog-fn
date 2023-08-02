(ns monkey.blog.fe.views
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.journal :as journal]
            [monkey.blog.fe.login.views :as login]
            [monkey.blog.fe.search :as search]
            [monkey.blog.fe.uploads.views :as uploads]
            [monkey.blog.fe.blog.views :as blog]
            [monkey.blog.fe.drafts.views :as drafts]))

(defn main-panel []
  (let [auth? (rf/subscribe [:authenticated?])
        panel (rf/subscribe [:current-panel])
        public? #(or (nil? %) (#{:home} %))
        p (:panel @panel)]
    (if (not (or @auth? (public? p)))
      ;; User wants access to secure area, show login panel
      [login/login-panel]
      ;; User has either been authenticated, or the area is public
      (case p
        :journal
        (->> (:params @panel)
             (into [journal/overview]))
        :journal/edit
        [journal/edit]
        :journal/view
        [journal/view]
        :journal/search
        [search/search-form]
        :uploads
        [uploads/new-upload]
        :uploads/list
        [uploads/list-uploads]
        :drafts
        [drafts/draft-overview]
        :draft/edit
        [drafts/edit-draft]
        ;; Default
        [blog/bliki]))))
