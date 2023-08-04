(ns monkey.blog.fe.test.helpers
  (:require [re-frame.core :as rf]))

(defn catch-fx [fx]
  (let [e (atom [])]
    (rf/reg-fx fx (fn [v] (swap! e conj v)))
    e))

(defn simulate-fx [fx]
  (rf/reg-fx fx (constantly nil)))

(defn catch-http []
  (let [e (atom [])]
    (rf/reg-fx :dispatch
               (fn [[t & req]]
                 (when (= :martian.re-frame/request t)
                   (swap! e conj req))))
    e))

(defn simulate-http []
  ;; Noop
  )
