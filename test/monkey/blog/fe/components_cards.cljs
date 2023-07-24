(ns monkey.blog.components-cards
  (:require [devcards.core :refer-macros [defcard-rg]]
            [reagent.core]
            [re-frame.db :as rdb]
            [monkey.blog.alerts :as a]
            [monkey.blog.components :as sut]))

(defcard-rg notification
  "Display notification"
  [:div.notification "Test notification"])

(defcard-rg error
  "Display error"
  [sut/error {:message "Test error"}])

(defcard-rg error-with-retry
  "Adds retry link"
  [sut/error {:message "Test error"
              :retry [:retry-evt]}])

(defcard-rg public-links
  "Displays public links"
  [sut/public-links])

(defcard-rg private-links
  "Displays private links"
  [sut/private-links])
