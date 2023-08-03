(ns monkey.blog.fe.test.login.events-test 
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.login.events :as sut]
            [monkey.blog.fe.test.helpers :as h]
            [monkey.blog.fe.test.fixtures :as tf]))

(use-fixtures :each tf/reset-db)

(deftest login-username
  (testing "sets username in db"
    (rf/dispatch-sync [:login/username "test-username"])
    (is (= "test-username" (-> (db/credentials @app-db)
                               :username)))))

(deftest login-password
  (testing "sets password in db"
    (rf/dispatch-sync [:login/password "test-password"])
    (is (= "test-password" (-> (db/credentials @app-db)
                               :password)))))

(deftest login
  (testing "sends credentials to backend using basic auth"
    (let [e (h/catch-http)]
      (is (some? (reset! app-db (db/set-credentials {} :test-creds))))
      (rf/dispatch-sync [:login])
      (is (= :login (ffirst @e))))))

(deftest login-succeeded
  (testing "does not change current panel"
    (is (some? (reset! app-db (db/set-current-panel {} :test-panel []))))
    (rf/dispatch-sync [:login/succeeded "OK"])
    (is (= :test-panel (:panel (db/current-panel @app-db)))))

  (testing "sets authenticated in db"
    (rf/dispatch-sync [:login/succeeded "OK"])
    (is (true? (db/authenticated? @app-db)))))

(deftest login-failed
  (testing "reports errors"
    (rf/dispatch-sync [:login/failed])
    (is (string? (db/error @app-db)))))

(deftest login-logoff
  (testing "sends request to backend" 
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:login/logoff])
      (is (= :logoff (ffirst @e))))))

(deftest logoff-success
  (let [r (h/catch-fx :goto)]
    (testing "redirects to home page"
      (rf/dispatch-sync [:logoff/success "ok"])
      (is (= ["#/"] @r)))

    (testing "resets errors"
      (is (some? (reset! app-db (db/set-error {} "test error"))))
      (rf/dispatch-sync [:login/logoff])
      (is (nil? (db/error @app-db))))))

(deftest logoff-failed
  (testing "reports errors"
    (rf/dispatch-sync [:logoff/failed])
    (is (string? (db/error @app-db)))))
