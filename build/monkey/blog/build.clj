(ns monkey.blog.build
  (:require [monkey.build :as mb]))

(defn uberjar [{:keys [version] :as args}]
  (println "Building uberjar for version" version)
  (mb/uberjar args))
