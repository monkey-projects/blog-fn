(ns monkey.blog.fe.location
  "Cofx handler for JavaScript browser location"
  (:require [re-frame.core :as rf]))

(rf/reg-cofx
 :location
 (fn [cofx _]
   (let [loc js/location]
     (assoc cofx :location {:host (.-host loc)
                            :path (.-pathname loc)}))))
