(ns monkey.blog.fe.db
  (:require [monkey.blog.fe.alerts :as a]))

(defn current-panel [db]
  (::current-panel db))

(defn set-current-panel [db p & [params]]
  (assoc db ::current-panel {:panel p
                             :params params}))

(defn journal-period [db]
  (:journal/period db))

(defn set-journal-period [db p]
  (assoc db :journal/period p))

(defn journal-entries [db]
  (:journal/entries db))

(defn set-journal-entries [db e]
  (assoc db :journal/entries e))

(defn get-journal-entry-by-id [db id]
  (->> (journal-entries db)
       (filter (comp (partial = (str id)) str :id))
       (first)))

(defn remove-journal-entry-by-id [db id]
  (update db :journal/entries (partial remove (comp (partial = (str id)) str :id))))

(defn journal-months [db]
  (:journal/months db))

(defn set-journal-months [db m]
  (assoc db :journal/months m))

(defn current-journal [db]
  (:journal/current db))

(defn set-current-journal [db e]
  (assoc db :journal/current e))

(defn journal-year-expanded? [db y]
  (some? ((or (:journal/expanded-years db) #{}) y)))

(defn toggle-journal-year [db y]
  (update db :journal/expanded-years 
          (comp set (if (journal-year-expanded? db y) disj conj))
          y))

(defn credentials [db]
  (:login/credentials db))

(defn set-credentials [db creds]
  (assoc db :login/credentials creds))

(defn authenticated? [db]
  (true? (:login/authenticated? db)))

(defn set-authenticated [db a]
  (assoc db :login/authenticated? a))

(defn set-error [db e]
  (a/set-error db e))

(defn clear-error [db]
  (a/clear-error db))

(defn error [db]
  (a/error db))

(defn filter-words [db]
  (:journal/filter-words db))

(defn set-filter-words [db words]
  (assoc db :journal/filter-words words))

(defn search-results [db]
  (:journal/search-results db))

(defn set-search-results [db r]
  (assoc db :journal/search-results r))

(defn set-uploads [db u]
  (assoc db :file/uploads u))

(defn uploads [db]
  (get db :file/uploads))

(defn set-last-upload [db u]
  (assoc db :file/last-upload u))

(defn last-upload [db]
  (:file/last-upload db))

(defn set-file-area [db a]
  (assoc db :file/area a))

(defn file-area [db]
  (:file/area db))
