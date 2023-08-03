(ns monkey.blog.fe.test.tags-test
  (:require #?@(:clj  [[clojure.test :refer :all]]
                :cljs [[cljs.test :refer-macros [testing deftest is] :refer [use-fixtures]]])
            [monkey.blog.fe.tags :as sut]))

(deftest parse-tags
       (testing "wraps untagged text in a span"
         (is (= [:span "Untagged text"]
                (sut/parse-tags "Untagged text"))))
       
       (testing "replaces $(italic) with <i>"
         (is (= [:span "This is text with " [:i "italic parts"]]
                (sut/parse-tags "This is text with $(italic italic parts)"))))
       
       (testing "replaces $(bold) with <b>"
         (is (= [:span "This is text with " [:b "bold parts"]]
                (sut/parse-tags "This is text with $(bold bold parts)"))))
       
       (testing "handles mailto without arguments"
             (is (= [:span "Send an email to " [:a {:href "mailto:wout@neirynck.com"} "wout@neirynck.com"]]
                    (sut/parse-tags "Send an email to $(mailto wout@neirynck.com)"))))

       (testing "handles mailto with arguments"
         (is (= [:span"Send an email to " [:a {:href "mailto:wout@neirynck.com"} "Wout Neirynck"]]
                (sut/parse-tags "Send an email to $(mailto wout@neirynck.com Wout Neirynck)"))))
       
       (testing "handles external links"
             (is (= [:span "Link to " [:a {:href "http://www.neirynck.com"
                                       :target "_blank"}
                                       "My site"]]
                    (sut/parse-tags "Link to $(link http://www.neirynck.com My site)"))))
       
       (testing "handles internal links"
         (is (= [:span "Link to " [:a {:href "#/journal/view/1234"} "this entry"]]
                (sut/parse-tags "Link to $(ref 1234 this entry)"))))
       
       (testing "handles images without options"
             (is (= [:span "This is an image: " [:a {:href "image/123"
                                                 :target "_blank"}
                                                 [:img {:src "image/123"}]]]
                    (sut/parse-tags "This is an image: $(img 123)"))))
       
       (testing "handles images with options"
             (is (= [:span "This is an image: " [:a {:href "image/123"
                                                 :target "_blank"}
                                                 [:img {:src "image/123?width=100&height=200"}]]]
                    (sut/parse-tags "This is an image: $(img 123 :width 100 :height 200)"))))

       (testing "handles invalid tags"
         (let [p (sut/parse-tags "This is $(invalid an invalid tag)")]
           (is (= [:span "This is "] (take 2 p)))
           (is (= :b (-> p (nth 2) (first))))
           (is (map? (-> p (nth 2) (second))))
           (is (= "$(invalid an invalid tag)" (-> p (nth 2) (nth 2)))))))

(deftest split-paragraphs
       (testing "replaces newlines with breaks"
         (is (= [[:p "Line 1" [:br] "Line 2"]]
                (sut/split-paragraphs "Line 1\nLine 2"))))

       (testing "wraps paragraphs in `:p` tags"
         (is (= [[:p "Line 1"]
                 [:p "Line 2"]]
                (sut/split-paragraphs "Line 1\n\nLine 2")))))

(deftest raw->html
  (testing "converts raw text into html paragraph structure"
    (is (= [[:p [:span "test string"]]]
           (sut/raw->html "test string"))))

  (testing "uses breaks for newlines"
    (is (= [[:p [:span "multiple"] [:br] [:span "lines"]]]
           (sut/raw->html "multiple\nlines"))))

  (testing "handles multiple paragraphs with multiple lines"
    (is (= [[:p [:span "multiple"]]
            [:p
             [:span "paragraphs with"]
             [:br]
             [:span "multiple lines"]]]
           (sut/raw->html
            "multiple\n\nparagraphs with\nmultiple lines"))))

  (testing "handles markup"
    (is (= [[:p [:span "text with " [:i "some markup"]]]]
           (sut/raw->html
            "text with $(i some markup)")))))
