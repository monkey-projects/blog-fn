(ns monkey.blog.fe.time
  "Front-end time related functions using Luxon"
  (:require ["luxon" :refer (DateTime)]
            [monkey.blog.fe.utils :as u]))

(defn datetime? [x]
  (and (some? x) (some? (.-toLocaleString x))))

(defn parse-date-time [s]
  (if (datetime? s)
    s
    (DateTime.fromISO s)))

(defn- ->time [s]
  (parse-date-time s))

(defn make-date [y m d]
  (DateTime.fromObject (clj->js {:year y
                                 :month m
                                 :day d})))

(defn today []
  (DateTime.now))

(defn format-date-time [dt]
  (when dt
    (.toLocaleString dt DateTime.DATETIME_MED_WITH_WEEKDAY)))

(defn format-month [dt]
  (when dt
    (.toLocaleString dt (clj->js {:month "long" :year "numeric"}))))

(defn- sign [x]
  (if (neg? x) "-" "+"))

(defn tz-offset
  "Retrieves current timezone offset"
  []
  (let [o (-> (js/Date.)
              (.getTimezoneOffset)
              (-))
        h (int (/ o 60))
        m (mod o 60)]
    (str (sign h) (u/pad-zero (u/abs h) 2) ":" (u/pad-zero m 2))))
