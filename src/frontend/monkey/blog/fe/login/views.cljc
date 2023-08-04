(ns monkey.blog.fe.login.views
  "Login form views"
  (:require [re-frame.core :as rf]
            [monkey.blog.fe.comps :as c]
            [monkey.blog.fe.utils :as u]))

(defn login-panel []
  (let [creds (rf/subscribe [:login/credentials])]
    [:div.login
     [:p "Please login to access my private parts."]
     [:div.entry
      [:div.title "authenticate"]
      [:form
       [:div.form-entry
        [:div "username"]
        [:input {:type :text
                 :id :username
                 :value (or (:username @creds) "")
                 :on-change (u/value-handler [:login/username])}]]
       [:div.form-entry
        [:div "password"]
        [:input {:type :password
                 :id :password
                 :value (or (:password @creds) "")
                 :on-change (u/value-handler [:login/password])}]]
       [:div.form-entry
        ;; use a button that looks like a link, this allows submitting the form by pressing enter
        [:button.link-btn
         {:on-click (u/evt-dispatch-handler [:login])} "login"]
        " | "
        [:a {:href "#/"} "cancel"]]
       [c/error]]]]))
