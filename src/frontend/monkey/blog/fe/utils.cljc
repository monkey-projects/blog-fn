(ns monkey.blog.fe.utils
  (:refer-clojure :exclude [abs])
  (:require [re-frame.core :as rf]))

(defn evt-dispatch-handler [evt]
  (fn [e]
    #?(:cljs (.preventDefault e))
    (rf/dispatch evt)))

(defn- get-value [e]
  #?(:cljs (.-value (.-target e))
     :clj (get-in e [:target :value])))

(defn value-handler
  "Returns an event handler that dispatches to `evt` with the value
   of the event attached."
  [evt]
  (fn [e]
    (rf/dispatch-sync (vec (conj evt (get-value e))))))

(defn extract-error [e]
  (or (:status-text e)
      (str e)))

(defn pad-left
  "Left-pads given string with the number of characters"
  [x c w]
  (str (->> (repeat (- w (count (str x))) c)
            (apply str))
       x))

(defn pad-zero
  "Left-pads number x with zeroes up to width w"
  [x w]
  (pad-left (str x) "0" w))

(defn abs [x]
  #?(:cljs (cljs.core/abs x)
     :clj (Math/abs x)))

(defn maybe-update [m k f & args]
  (if (contains? m k)
    (apply update m k f args)
    m))

    
