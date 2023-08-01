(ns monkey.blog.test.persist.file-storage-test
  (:require [clojure.test :refer :all]
            [monkey.blog.persist :as p]
            [monkey.blog.persist.file-storage :as sut])
  (:import java.io.File
           org.apache.commons.io.FileUtils))

(def temp-dir (atom nil))

(defn- make-temp-dir []
  (let [dir (File. (FileUtils/getTempDirectory) (str "storage-" (random-uuid)))]
    (.mkdir dir)
    dir))

(defn- delete-dir [d]
  (FileUtils/deleteDirectory d))

(defn- temp-dir-fixture [f]
  (let [t (make-temp-dir)]
    (reset! temp-dir t)
    (try
      (f)
      (finally
        (delete-dir t)))))

(use-fixtures :each temp-dir-fixture)

(defn- count-files []
  (count (.listFiles @temp-dir)))

(deftest file-storage
  (let [st (sut/make-file-storage @temp-dir)]
    (testing "list-entries"
      
      (testing "empty if no entries"
        (is (empty? (p/list-entries st {}))))

      (testing "can write and list entry"
        (let [id (p/write-entry st {:title "test entry"
                                    :contents "This is a test"})]
          (is (some? id))
          (is (= 1 (count (p/list-entries st {:id id})))))))

    (testing "can write and delete"
      (let [n (count-files)
            id (p/write-entry st {:title "another test"
                                  :contents "This is another test"})]
        (is (some? id))
        (is (= (inc n) (count-files)) "Expected number of files to increase by one")
        (is (= id (p/delete-entry st id)))
        (is (= n (count-files)) "Expected number of files to be same as initial")))

    (testing "read-entry"
      (testing "can read existing entries"
        (let [id (p/write-entry st {:title "another test"
                                    :contents "This is yet another test"})
              r (p/read-entry st id)]
          (is (some? r))
          (is (= "another test" (:title r)))
          (is (= id (:id r)))))

      (testing "`nil` if entry does not exist"
        (is (nil? (p/read-entry st (str (random-uuid)))))))))
