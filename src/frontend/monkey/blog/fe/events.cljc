(ns monkey.blog.fe.events
  (:require [clojure.string :as cs]
            [monkey.blog.fe.location]
            [re-frame.core :as rf]))

(defn- strip-index [s]
  (cs/replace s #"/index\.html$" ""))

(rf/reg-event-fx
 :initialize-db
 [(rf/inject-cofx :location)]
 (fn [{:keys [location db]} _]
   {:db (assoc db :base-path (-> (:path location)
                                 (strip-index)))}))
