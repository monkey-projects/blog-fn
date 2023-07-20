(ns monkey.blog.build
  (:require [clojure.tools.build.api :as b]
            [monkey.build :as mb]))

(defn uberjar [{:keys [version] :as args}]
  (println "Building uberjar for version" version)
  (mb/uberjar args))

(defn war
  "Builds WAR file for AppEngine deployment"
  [{:keys [jar war] :or {war "target/app.war"}}]
  (let [target "target/war"]
    (println "building .war file for AppEngine deployment to" war)
    (b/copy-dir {:src-dirs ["resources/public"]
                 :target-dir target})
    (b/copy-dir {:src-dirs ["resources/WEB-INF"]
                 :target-dir (str target "/WEB-INF")})
    (b/copy-file {:src jar
                  :target (str target "/WEB-INF/lib/uber.jar")})
    (b/jar {:class-dir target
            :jar-file war})))
