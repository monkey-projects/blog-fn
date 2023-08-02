(ns monkey.blog.fe.test.helpers
  (:require [re-frame.core :as rf]))

(defn catch-fx [fx]
  (let [e (atom [])]
    (rf/reg-fx fx (fn [v] (swap! e conj v)))
    e))

(defn simulate-fx [fx]
  (rf/reg-fx fx (constantly nil)))

(defn catch-http []
  (catch-fx :http-xhrio))
