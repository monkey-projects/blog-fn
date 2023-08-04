(ns monkey.blog.fe.uploads.events
  (:require [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.utils :as u]))

(defn- get-files-from-field
  "Gets the files from the given file field"
  [id]
  #?(:cljs
     (let [file-list (.-files (js/document.getElementById (name id)))]
       (mapv #(aget file-list %) (range (.-length file-list))))))

(rf/reg-cofx
 :file/files-to-form-data
 ;; Takes upload files from another cofx handler and converts them to a form data object
 ;; that can then be sent in multipart requests.
 ;; Also provides a function that can be invoked to add additional fields to the form data.
 (fn [co [field name]]
   #?(:cljs
      ;; For now, only one file supported
      (let [fd (js/FormData.)
            upload-files (->> (get-files-from-field field)
                              (take 1))]
        (doseq [f upload-files]
          (.append fd name f))
        (assoc co
               :form-data fd
               :add-form-data (fn [fd k v]
                                (.append fd k v)
                                fd)))
      :clj (assoc co
                  :form-data :test-data
                  :add-form-data (fn [fd & args] fd)))))

(rf/reg-event-fx
 :file/list-uploads
 (fn [_ _]
   {::martian/request [:list-uploads
                       {}
                       [:file/uploads-loaded]
                       [:file/list-uploads-failed]]}))

(rf/reg-event-db
 :file/uploads-loaded
 (fn [db [_ uploads]]
   (db/set-uploads db uploads)))

(rf/reg-event-db
 :file/list-uploads-failed
 (fn [db [_ error]]
   (db/set-error db (str "Failed to retrieve uploads: " (u/extract-error error)))))

(rf/reg-event-fx
 :file/upload
 [(rf/inject-cofx :file/files-to-form-data ["file" "file"])]
 (fn [{:keys [db] :as ctx} _]
   (let [add (:add-form-data ctx)]
     {::martian/request [:upload-file
                         (-> (:form-data ctx)
                             (add "area" (or (db/file-area db) "journal")))
                         [:file/upload-succeeded]
                         [:file/upload-failed]]
      :db (db/clear-error db)})))

(rf/reg-event-db
 :file/upload-failed
 (fn [db [_ error]]
   (db/set-error db (str "Failed to upload file: " (u/extract-error error)))))

(rf/reg-event-db
 :file/upload-succeeded
 (fn [db [_ u]]
   (db/set-last-upload db u)))

(rf/reg-event-db
 :file/area-changed
 (fn [db [_ a]]
   (db/set-file-area db a)))
