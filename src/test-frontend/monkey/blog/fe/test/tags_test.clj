(ns monkey.blog.fe.tags-test
  (:require [monkey.blog.fe.tags :as sut]
            [midje.sweet :refer :all]))

(facts "about `parse-tags`"
       (fact "wraps untagged text in a span"
             (sut/parse-tags "Untagged text") => [:span "Untagged text"])
       
       (fact "replaces $(italic) with <i>"
             (sut/parse-tags "This is text with $(italic italic parts)")
             => [:span "This is text with " [:i "italic parts"]])
       
       (fact "replaces $(bold) with <b>"
             (sut/parse-tags "This is text with $(bold bold parts)")
             => [:span "This is text with " [:b "bold parts"]])
       
       (fact "handles mailto without arguments"
             (sut/parse-tags "Send an email to $(mailto wout@neirynck.com)")
             => [:span "Send an email to " [:a {:href "mailto:wout@neirynck.com"} "wout@neirynck.com"]])

       (fact "handles mailto with arguments"
             (sut/parse-tags "Send an email to $(mailto wout@neirynck.com Wout Neirynck)")
             => [:span"Send an email to " [:a {:href "mailto:wout@neirynck.com"} "Wout Neirynck"]])
       
       (fact "handles external links"
             (sut/parse-tags "Link to $(link http://www.neirynck.com My site)")
             => [:span "Link to " [:a {:href "http://www.neirynck.com"
                                       :target "_blank"}
                                   "My site"]])
       
       (fact "handles internal links"
             (sut/parse-tags "Link to $(ref 1234 this entry)") =>
             [:span "Link to " [:a {:href "#/journal/view/1234"} "this entry"]])
       
       (fact "handles images without options"
             (sut/parse-tags "This is an image: $(img 123)")
             => [:span "This is an image: " [:a {:href "image/123"
                                                 :target "_blank"}
                                             [:img {:src "image/123"}]]])
       
       (fact "handles images with options"
             (sut/parse-tags "This is an image: $(img 123 :width 100 :height 200)")
             => [:span "This is an image: " [:a {:href "image/123"
                                                 :target "_blank"}
                                             [:img {:src "image/123?width=100&height=200"}]]])

       (fact "handles invalid tags"
             (sut/parse-tags "This is $(invalid an invalid tag)")
             => (just [:span "This is " (just [:b map? "$(invalid an invalid tag)"])])))

(facts "about `split-paragraphs`"
       (fact "replaces newlines with breaks"
             (sut/split-paragraphs "Line 1\nLine 2") => [[:p "Line 1" [:br] "Line 2"]])

       (fact "wraps paragraphs in `:p` tags"
             (sut/split-paragraphs "Line 1\n\nLine 2") => [[:p "Line 1"]
                                                           [:p "Line 2"]]))

(facts "about `raw->html`"
       (fact "converts raw text into html paragraph structure"
             (sut/raw->html "test string") => [[:p [:span "test string"]]]
             (sut/raw->html "multiple\nlines") => [[:p [:span "multiple"] [:br] [:span "lines"]]]
             (sut/raw->html
              "multiple\n\nparagraphs with\nmultiple lines") => [[:p [:span "multiple"]]
                                                                 [:p
                                                                  [:span "paragraphs with"]
                                                                  [:br]
                                                                  [:span "multiple lines"]]]
             (sut/raw->html
              "text with $(i some markup)") => [[:p [:span "text with " [:i "some markup"]]]]))
