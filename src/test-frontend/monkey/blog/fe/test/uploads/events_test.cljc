(ns monkey.blog.fe.test.uploads.events-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [monkey.blog.fe.uploads.events :as sut]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.test.helpers :as h]
            [monkey.blog.fe.test.fixtures :as tf]))

(use-fixtures :each tf/reset-db)

(deftest file-list-uploads
  (testing "sends request to backend"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:file/list-uploads])
      (is (= :list-uploads (ffirst @e))))))

(deftest file-list-uploads-failed
  (testing "sets error"
    (rf/dispatch-sync [:file/list-uploads-failed "test error"])
    (is (= (re-matches #".*test error.*" (db/error @app-db))))))

(deftest file-uploads-loaded
  (testing "sets uploads in db"
    (rf/dispatch-sync [:file/uploads-loaded :test-uploads])
    (is (= :test-uploads (db/uploads @app-db)))))

(deftest file-upload
  (rf/reg-cofx :file/files-to-form-data (fn [co _]
                                          (assoc co
                                                 :form-data :test-data
                                                 :add-form-data (fn [fd & args] fd))))
  
  (testing "sends request to backend"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:file/upload])
      (is (= :upload-file (ffirst @e)))))
  
  (testing "adds form data as body"
    (let [e (h/catch-http)]
      (rf/dispatch-sync [:file/upload])
      (is (some? (-> @e (first) (second))))))

  (testing "clears errors"
    (h/simulate-http)
    (is (some? (reset! app-db (db/set-error {} "test error"))))
    (rf/dispatch-sync [:file/upload])
    (is (nil? (db/error @app-db)))))

(deftest file-upload-succeeded
  (testing "sets last upload in db"
    (rf/dispatch-sync [:file/upload-succeeded :test-upload])
    (is (= :test-upload (db/last-upload @app-db)))))

(deftest file-upload-failed
  (testing "sets error"
    (rf/dispatch-sync [:file/upload-failed "test error"])
    (is (re-matches #".*test error.*" (db/error @app-db)))))

(deftest file-area-changed
  (testing "sets area in db"
    (rf/dispatch-sync [:file/area-changed "journal"])
    (is (= "journal" (db/file-area @app-db)))))
