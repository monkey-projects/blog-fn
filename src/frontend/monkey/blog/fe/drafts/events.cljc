(ns monkey.blog.fe.drafts.events
  (:require [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.utils :as u]
            [monkey.blog.fe.drafts.db :as db]
            [monkey.blog.fe.db :as cdb]
            [monkey.blog.fe.alerts :as a]))

(rf/reg-event-fx
 :drafts/load
 (fn [ctx _]
   {:dispatch [::martian/request
               :list-entries
               {:area "drafts"}
               [:drafts/loaded]
               [:drafts/load-failed]]}))

(rf/reg-event-db
 :drafts/loaded
 (fn [db [_ drafts]]
   (db/set-drafts db (or drafts []))))

(rf/reg-event-db
 :drafts/load-failed
 (fn [db [_ error]]
   (db/set-error db (u/extract-error error))))

(rf/reg-event-db
 :draft/edit
 (fn [db [_ id]]
   (-> (->> (db/drafts db)
            (filter #(= id (str (:id %))))
            (first)
            (db/set-current-draft db))
       (cdb/set-current-panel :draft/edit))))

(rf/reg-event-db
 :draft/new
 (fn [db _]
   (-> db
       (db/set-current-draft {})
       (cdb/set-current-panel :draft/edit))))

(rf/reg-event-fx
 :draft/delete
 (fn [{:keys [db]} [_ draft]]
   {:dispatch [::martian/request
               :delete-entry
               {:area "drafts"
                :id (:id draft)}
               [:draft/deleted]
               [:draft/delete-failed]]
    :db (a/clear-all db)}))

(rf/reg-event-db
 :draft/deleted
 (fn [db [_ draft]]
   (-> db
       (a/set-notification (str "Draft " (:id draft) " has been deleted"))
       (db/update-drafts (partial remove (partial = draft))))))

(rf/reg-event-db
 :draft/delete-failed
 (fn [db [_ error]]
   (a/set-error db (str "Failed to delete draft: " (u/extract-error error)))))

(rf/reg-event-fx
 :draft/save
 (fn [{:keys [db]} _]
   (let [d (db/current-draft db)
         new? (nil? (:id d))]
     {:dispatch [::martian/request
                 (if new? :create-entry :update-entry)
                 (-> d
                     (select-keys [:id :title :contents])
                     (merge {:area "drafts"}))
                 [:draft/saved]
                 [:draft/save-failed]]
      :db (a/clear-all db)})))

(defn- add-or-replace-draft [db {:keys [id] :as upd}]
  (if-let [match (db/draft-by-id db id)]
    (db/update-drafts db (partial replace {match upd}))
    (db/update-drafts db conj upd)))

(rf/reg-event-db
 :draft/saved
 (fn [db [_ upd]]
   (-> db
       (db/set-current-draft upd)
       (add-or-replace-draft upd)
       (a/set-notification (str"Draft " (:id upd) " was saved succesfully.")))))

(rf/reg-event-db
 :draft/save-failed
 (fn [db [_ error]]
   (a/set-error db (str "Failed to save draft: " (u/extract-error error)))))

(rf/reg-event-fx
 :draft/publish
 (fn [ctx [_ draft]]
   {:dispatch [::martian/request
               :publish-draft
               {:id (:id draft)}
               [:draft/published]
               [:draft/publish-failed]]}))

(rf/reg-event-db
 :draft/published
 (fn [db [_ draft entry]]
   (a/set-notification db (str "Draft " (:id draft) " has been published"))))

(rf/reg-event-db
 :draft/publish-failed
 (fn [db [_ error]]
   (a/set-error db (str "Failed to publish draft: " (u/extract-error error)))))

(rf/reg-event-db
 :draft/changed
 (fn [db [_ k v]]
   (db/set-current-draft db (assoc (db/current-draft db) k v))))
