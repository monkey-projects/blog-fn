(ns monkey.blog.fe.cookies
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(defn- parse-cookies [c]
  (->> (str/split c #";")
       (map str/trim)
       (map (fn [s]
              (str/split s #"=")))
       (map (fn [[k v]]
              [(keyword k) v]))
       (into {})))

(rf/reg-cofx
 :cookie
 ;; Retrieves cookie with given name, or all cookies
 (fn [coeffects n]
   (assoc coeffects :cookie (-> (.-cookie js/document)
                                (parse-cookies)))))
