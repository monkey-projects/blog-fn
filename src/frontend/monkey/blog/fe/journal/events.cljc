(ns monkey.blog.fe.journal.events
  (:require [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.config :refer [debug?]]
            [monkey.blog.fe.db :as gdb]
            [monkey.blog.fe.journal.db :as db]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.utils :as u]))

(rf/reg-event-fx
 :journal/load-months
 (fn [{:keys [db]} _]
   {:dispatch [::martian/request
               :list-months
               {:area "journal"}
               [:journal/months-loaded]
               [:journal/load-months-failed]]
    :db (a/set-notification db "Loading journal overview...")}))

(rf/reg-event-db
 :journal/months-loaded
 (fn [db [_ m]]
   (-> db
       (db/set-journal-months (:body m))
       (a/clear-notification))))

(rf/reg-event-db
 :journal/load-months-failed
 (fn [db [_ m]]
   (-> db
       (gdb/set-error "Failed to load journal months")
       (a/clear-notification))))

(rf/reg-event-fx
 :journal/set-period
 (fn [{:keys [db]} [_ p]]
   {:db (-> db
            (db/set-journal-period p)
            (gdb/set-current-panel :journal []))
    :dispatch [:journal/load-entries]}))

(rf/reg-event-fx
 :journal/load-entries
 (fn [{:keys [db]} _]
   (let [p (db/journal-period db)]
     {:dispatch [::martian/request
                 :list-entries
                 (cond-> {:area "journal"}
                   p (assoc :period p))
                 [:journal/entries-loaded]
                 [:journal/load-entries-failed]]
      :db (a/set-notification db (str "Loading entries for " p "..."))})))

(rf/reg-event-db
 :journal/entries-loaded
 (fn [db [_ e]]
   (-> db
       (db/set-journal-entries e)
       (a/clear-notification))))

(rf/reg-event-db
 :journal/load-entries-failed
 (fn [db [_ m]]
   (-> db
       (gdb/set-error "failed to load entries")
       (a/clear-notification))))

(rf/reg-event-db
 :journal/edit
 (fn [db [_ id]]
   (-> db
       (db/set-current-journal (db/get-journal-entry-by-id db id))
       (gdb/set-current-panel :journal/edit))))

(rf/reg-event-db
 :journal/new
 (fn [db _]
   (-> db
       (db/set-current-journal {})
       (gdb/set-current-panel :journal/edit))))

(rf/reg-event-fx
 :journal/view
 (fn [{:keys [db]} [_ id]]
   (let [curr (db/current-journal db)]
     (cond-> {:db (gdb/set-current-panel db :journal/view)}
       (not= (:id curr) id)
       (assoc :dispatch
              [::martian/request
               :get-entry
               {:id id}
               [:journal/entry-loaded]
               [:journal/entry-load-failed]])))))

(rf/reg-event-db
 :journal/entry-loaded
 (fn [db [_ e]]
   (db/set-current-journal db e)))

(rf/reg-event-db
 :journal/entry-load-failed
 (fn [db [_ error]]
   (gdb/set-error db (str "failed to load entry: " (u/extract-error error)))))

(rf/reg-event-fx
 :journal/save
 (fn [{:keys [db]} _]
   (let [{:keys [id] :as curr} (db/current-journal db)]
     {:dispatch [::martian/request
                 (if (some? id) :update-entry :create-entry)
                 (cond-> curr
                   true (u/maybe-update :created-on str (t/tz-offset))
                   (nil? id) (dissoc :id))
                 [:journal/save-succeeded]
                 [:journal/save-failed]]
      :db (-> db
              (a/set-notification "Saving...")
              (a/clear-error))})))

(rf/reg-event-fx
 :journal/save-succeeded
 (fn [{:keys [db]} [_ {:keys [id] :as upd}]]
   #_(gdb/set-current-panel db :journal)
   {:goto "#/journal"
    :db (-> db
            (db/set-journal-entries
             (replace {(db/get-journal-entry-by-id db id) upd} (db/journal-entries db)))
            (a/clear-notification))}))

(rf/reg-event-db
 :journal/save-failed
 (fn [db [_ error]]
   (-> db
       (gdb/set-error (str "Failed to save entry: " (u/extract-error error)))
       (a/clear-notification))))

(rf/reg-event-db
 :journal/changed
 (fn [db [_ v]]
   (as-> (db/current-journal db) x
     (assoc x :body v)
     (db/set-current-journal db x))))

(rf/reg-event-fx
 :journal/delete
 (fn [{:keys [db]} _]
   (let [id (:id (db/current-journal db))]
     {:dispatch [::martian/request
                 :delete-entry
                 {:id id
                  :area "journal"}
                 [:journal/delete-succeeded id]
                 [:journal/delete-failed id]]})))

(rf/reg-event-fx
 :journal/delete-succeeded
 (fn [{:keys [db]} [_ id]]
   {:goto "#/journal"
    :db (-> db
            (a/set-notification (str "Journal entry " id " has been deleted."))
            (db/set-current-journal nil)
            (db/remove-journal-entry-by-id id))}))

(rf/reg-event-db
 :journal/delete-failed
 (fn [db [_ id err]]
   (a/set-error db (str "Failed to delete journal entry " id ": " (u/extract-error err)))))

(rf/reg-event-db
 :journal/toggle-year
 (fn [db [_ y]]
   (db/toggle-journal-year db y)))

(rf/reg-event-db
 :journal/search-filter
 (fn [db [_ f]]
   (db/set-filter-words db f)))

(defn- parse-filter-words [db]
  (some-> (db/filter-words db)
          (clojure.string/split #" ")))

(rf/reg-event-fx
 :journal/search
 (fn [{:keys [db]} _]
   {:dispatch [::martian/request
               :search-entry
               {:words (parse-filter-words db)
                :area "journal"}
               [:journal/search-succeeded]
               [:journal/search-failed]]
    :db (gdb/clear-error db)}))

(rf/reg-event-db
 :journal/search-succeeded
 (fn [db [_ results]]
   (db/set-search-results db results)))

(rf/reg-event-db
 :journal/search-failed
 (fn [db [_ error]]
   (gdb/set-error db (str "Failed to search journal entries: " (u/extract-error error)))))
