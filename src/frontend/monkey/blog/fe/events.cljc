(ns monkey.blog.fe.events
  (:require [re-frame.core :as rf]
            [martian.re-frame :as martian]
            [monkey.blog.fe.alerts :as a]
            [monkey.blog.fe.config :refer [debug?]]
            [monkey.blog.fe.db :as db]
            [monkey.blog.fe.journal.db :as jdb]
            [monkey.blog.fe.time :as t]
            [monkey.blog.fe.utils :as u]))

(rf/reg-event-fx
 ::initialize-db
 [(rf/inject-cofx :cookie :bliki-session)]
 (fn [{:keys [db cookie]} _]
   {:db (cond-> {}
          true (db/set-authenticated (some? (:bliki-session cookie)))
          true (db/set-file-area "public")
          debug? (-> (jdb/set-journal-entries [{:id 1
                                                :created-on "2020-05-19 22:02"
                                                :body "This is a test entry"}])
                     (db/set-current-panel :journal)
                     (jdb/set-journal-months {:2020 (zipmap (range 1 5) (repeat 1))})))}))
