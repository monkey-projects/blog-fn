(ns user
  "Functions to use on the repl"
  (:require [monkey.blog.main :as m]))

(defonce server (atom nil))

(defn start-server []
  (swap! server (fn [s]
                  (when s
                    (s))
                  (m/-main))))

(defn stop-server []
  (swap! server (fn [s]
                  (when s
                    (s))
                  nil)))

(def restart start-server)
