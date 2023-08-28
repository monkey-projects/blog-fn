(ns monkey.blog.fe.time
  "Dummy implementation for time-related functions, for testing"
  (:require [java-time :as jt]))

(defn datetime? [x]
  (jt/offset-date-time? x))

(defn make-date [y m d]
  (jt/local-date y m d))

(defn today []
  (jt/local-date))

(defn parse-date-time [s]
  (when s
    (jt/offset-date-time s)))

(defn format-date-time [dt]
  (jt/format :iso-local-date-time dt))

(defn format-month [dt]
  (jt/format (jt/formatter "MMMM YYYY") dt))

(defn tz-offset []
  "+02:00")
