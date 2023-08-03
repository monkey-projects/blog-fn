(ns monkey.blog.fe.test.drafts.events-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.test.helpers :as h]
            [monkey.blog.fe.test.fixtures :as tf]
            [monkey.blog.fe.drafts.db :as db]
            [monkey.blog.fe.drafts.events :as sut]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.db :as cdb]))

(use-fixtures :each tf/reset-db)

(deftest drafts-load
  (testing "loads all from backend"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:drafts/load])
      (is (= :list-entries (ffirst @e)))
      (is (= {:area "drafts"} (-> @e (first) (second)))))))

(deftest drafts-loaded
  (testing "stores drafts in db"
    (rf/dispatch-sync [:drafts/loaded :test-drafts])
    (is (= :test-drafts (db/drafts @app-db)))))

(deftest draft-edit
  (testing "gets draft from local db"
    (is (some? (reset! app-db (db/set-drafts {} [{:id 12 :title "test draft"}]))))
    (rf/dispatch-sync [:draft/edit "12"])
    (is (= "test draft" (-> (db/current-draft @app-db) :title))))

  (testing "sets current panel to draft/edit"
    (is (some? (reset! app-db (db/set-drafts {} [{:id 17 :title "test draft"}]))))
    (rf/dispatch-sync [:draft/edit "17"])
    (is (= :draft/edit (:panel (cdb/current-panel @app-db))))))

(deftest draft-publish
  (testing "sends request to backend"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:draft/publish {:id "test-draft"}])
      (is (= :publish-draft (ffirst @e))))))

(deftest draft-published
  (testing "sets notification"
    (rf/dispatch-sync [:draft/published :test-draft {:id :test-entry}])
    (is (string? (a/notification @app-db)))))

(deftest draft-publish-failed
  (testing "sets error"
    (rf/dispatch-sync [:draft/publish-failed :test-error])
    (is (some? (a/error @app-db)))))

(deftest draft-save
  (testing "saves new draft in backend"
    (let [e (h/catch-http)]
      (is (some? (reset! app-db (db/set-current-draft {} {:title "title"
                                                          :contents "test body"}))))
      (rf/dispatch-sync [:draft/save])
      (is (= :create-entry (ffirst @e)))))

  (testing "saves existing draft in backend"
    (let [e (h/catch-http)
          d {:id 12
             :title "title"
             :contents "test body"}]
      (is (some? (reset! app-db (db/set-current-draft {} d))))
      (rf/dispatch-sync [:draft/save])
      (is (= :update-entry (ffirst @e)))
      (is (= (assoc d :area "drafts") (-> @e (first) (second)))))))

(deftest draft-saved
  (testing "replaces current draft"
    (is (some? (reset! app-db (db/set-current-draft {} :test-draft))))
    (rf/dispatch-sync [:draft/saved :updated])
    (is (= :updated (db/current-draft @app-db))))

  (testing "sets notification"
    (rf/dispatch-sync [:draft/saved :updated])
    (is (some? (a/notification @app-db))))

  (testing "updates existing draft in list"
    (let [d {:id :test-id :title "test draft"}]
      (is (some? (reset! app-db (db/set-drafts {} [{:id :test-id}]))))
      (rf/dispatch-sync [:draft/saved d])
      (is (= [d] (db/drafts @app-db)))))

  (testing "adds new draft to list"
    (let [fd {:id :first}
          sd {:id :second :title "test draft"}]
      (is (some? (reset! app-db (db/set-drafts {} [fd]))))
      (rf/dispatch-sync [:draft/saved sd])
      (is (= [fd sd] (db/drafts @app-db))))))

(deftest draft-save-failed
  (testing "sets error"
    (rf/dispatch-sync [:draft/save-failed :test-error])
    (is (some? (a/error @app-db)))))

(deftest draft-delete
  (testing "deletes in backend"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:draft/delete {:id 123}])
      (is (= :delete-entry (ffirst @e)))
      (is (= {:id 123
              :area "drafts"}
             (-> @e (first) (second)))))))

(deftest draft-deleted
  (testing "sets notification"
    (rf/dispatch-sync [:draft/deleted :test-draft])
    (is (some? (a/notification @app-db))))

  (testing "removes draft from list"
    (is (some? (reset! app-db (db/set-drafts {} [{:id :first}
                                                 {:id :second}]))))
    (rf/dispatch-sync [:draft/deleted {:id :second}])
    (is (= [{:id :first}] (db/drafts @app-db)))))

(deftest draft-delete-failed
  (testing "sets error"
    (rf/dispatch-sync [:draft/delete-failed :test-error])
    (is (some? (a/error @app-db)))))

(deftest draft-changed
  (testing "updates draft property"
    (is (some? (reset! app-db (db/set-current-draft {} {}))))
    (rf/dispatch-sync [:draft/changed :title "test title"])
    (is (= "test title" (-> (db/current-draft @app-db)
                            :title)))))

(deftest draft-new
  (testing "sets current draft"
    (rf/dispatch-sync [:draft/new])
    (is (map? (db/current-draft @app-db))))

  (testing "sets current panel"
    (rf/dispatch-sync [:draft/new])
    (is (= :draft/edit (:panel (cdb/current-panel @app-db))))))
