(ns monkey.blog.fe.journal.db)

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

(defn filter-words [db]
  (:journal/filter-words db))

(defn set-filter-words [db words]
  (assoc db :journal/filter-words words))

(defn search-results [db]
  (:journal/search-results db))

(defn set-search-results [db r]
  (assoc db :journal/search-results r))
