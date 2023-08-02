(ns monkey.blog.fe.tags
  "Functions for converting tagged strings into a hiccup-style structure"
  (:require [clojure.string :as string]
            [clojure.walk :as w]))

(defmulti replace-tag second)

(defmethod replace-tag "italic" [[_ _ content]]
  [:i content])

(defmethod replace-tag "i" [[_ _ content]]
  [:i content])

(defmethod replace-tag "bold" [[_ _ content]]
  [:b content])

(defn- first-and-rest [content]
  (let [words (.split content " ")
        start (first words)
        desc (if-let [r (next words)]
               (string/join " " r)
               start)]
    [start desc]))

(defmethod replace-tag "mailto" [[_ _ content]]
  (let [[email desc] (first-and-rest content)]
    [:a {:href (str "mailto:" email)} desc]))

(defmethod replace-tag "link" [[_ _ content]]
  (let [[url desc] (first-and-rest content)]
    [:a {:href url :target "_blank"} desc]))

(defmethod replace-tag "ref" [[_ _ content]]
  (let [[id desc] (first-and-rest content)]
    [:a {:href (str "#/journal/view/" id)} desc]))

(defn- option? [s]
  (.startsWith s ":"))

(defn- parse-options [opts]
  (->> opts
    (partition 2)
    (filter (comp option? first))))

(defn- options-to-path [opts]
  (if-not (empty? opts)
    (str "?"
         (->> opts
              (map (fn [[k v]]
                     (str (subs k 1) "=" v)))
              (string/join "&")))
    ""))

(defmethod replace-tag "img" [[_ _ content]]
  (let [parts (.split content " ")
        id (first parts)
        opts (parse-options (rest parts))
        path (options-to-path opts)]
    [:a {:href (str "image/" id) :target "_blank"} [:img {:src (str "image/" id path)}]]))

(defmethod replace-tag :default [[m & _]]
  [:b {:title "malformed tag"} m])

(defn parse-tags [s]
  (let [regex #"\$\((\S+) ([^\)]*)\)"]
    (loop [in s
           out []]
      (if-let [[full f args :as r] (re-find regex in)]
        ;; If match found, process it and move to next
        (let [idx (.indexOf in full)]
          (recur (subs in (+ idx (count full)))
                 (vec (concat out [(subs in 0 idx) (replace-tag [full f args])]))))
        ;; Done
        (->> (conj out in)
             (remove empty?)
             (into [:span]))))))

(defn- split-lines [l]
  (->> (clojure.string/split l #"\n")
       (interpose [:br])))

(defn split-paragraphs [s]
  (->> (clojure.string/split s #"\n\n")
       (mapv split-lines)
       (mapv (partial into [:p]))))

(defn- maybe-parse-tags [v]
  (cond-> v
    (string? v) (parse-tags)))

(defn raw->html [s]
  (->> (split-paragraphs s)
       (w/postwalk maybe-parse-tags)))
