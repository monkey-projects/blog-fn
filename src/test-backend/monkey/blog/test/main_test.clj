(ns monkey.blog.test.main-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [monkey.blog
             [main :as sut]
             [persist :as p]]
            [org.httpkit.server :as http]
            [ring.mock.request :as mock]))

(def storage (p/make-memory-storage))
(def test-handler (sut/make-handler {:storage storage}))

(defn- status= [expected r]
  (= expected (:status r)))

(def not-found?  (partial status= 404))
(def success?    (partial status= 200))
(def created?    (partial status= 201))
(def no-content? (partial status= 204))

(defn json-> [in]
  (some-> in
          (io/reader)
          (json/read :key-fn keyword)))

(defn- clean-db [f]
  (reset! (:store storage) nil)
  (f))

(use-fixtures :each clean-db)

(deftest handler
  (testing "is a fn"
    (is (fn? test-handler)))

  (testing "GET /health"
    (is (success? (-> (mock/request :get "/health")
                      (test-handler)))))

  (testing "404 on unknown path"
    (is (not-found? (-> (mock/request :get "/unknown")
                        (test-handler)))))

  (testing "/api/entries"
    (testing "GET /:id"
      (testing "returns 404 when not found"
        (is (not-found? (-> (mock/request :get (str "/api/entries/blog/" (random-uuid)))
                            (test-handler)))))
      
      (testing "retrieves by id"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})
              response (-> (mock/request :get (str "/api/entries/blog/" id))
                           (test-handler))]
          (is (success? response))
          (is (= "test" (-> response
                            :body
                            (json->)
                            :title)))))
      
      (testing "not found if wrong area"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})]
          (is (not-found? (-> (mock/request :get (str "/api/entries/other/" id))
                              (test-handler)))))))

    (testing "GET /"
      (let [area "test"]
        (testing "404 if no entries"
          (is (not-found? (-> (mock/request :get (str "/api/entries/" area))
                              (test-handler)))))

        (testing "returns entries for area"
          (is (some? (p/write-entry storage {:title "test entry"
                                             :area area})))
          (is (success? (-> (mock/request :get (str "/api/entries/" area))
                            (test-handler)))))))

    (testing "POST /"
      (let [r (-> (mock/request :post "/api/entries/blog")
                  (mock/json-body {:title "test"
                                   :contents "Test item"})
                  (test-handler))]
        (testing "creates new entry"
          (is (created? r)))

        (testing "assigns id"
          (is (some? (-> r
                         :body
                         (json->)
                         :id))))))

    (testing "PUT /:id"
      (let [id (p/write-entry storage {:title "test"
                                       :area "blog"})
            r (-> (mock/request :put (str "/api/entries/blog/" id))
                  (mock/json-body {:title "test"
                                   :contents "Test item"})
                  (test-handler))]
        
        (testing "updates existing entry"
          (is (= "Test item" (-> (p/read-entry storage id)
                                 :contents))))

        (testing "returns success"
          (is (success? r)))

        (testing "returns updated entry"
          (is (= "Test item" (-> r
                                 :body
                                 (json->)
                                 :contents))))))

    (testing "DELETE /:id"
      (let [area "test-delete-area"]
        (testing "deletes by id"
          (let [id (p/write-entry storage {:title "To delete"
                                           :area area})]
            (is (some? id))
            (is (no-content? (-> (mock/request :delete (str "/api/entries/" area "/" id))
                                 (test-handler)))))))))

  (testing "api/latest"
    (testing "GET /:area"
      (testing "returns 404 when no entries"
        (let [h (sut/make-handler {:storage (p/make-memory-storage)})]
          (is (not-found? (-> (mock/request :get "/api/latest/blog")
                              (h))))))

      (testing "returns latest entry for area"
        (let [id (p/write-entry storage {:title "test"
                                         :area "blog"})
              response (-> (mock/request :get "/api/latest/blog")
                           (test-handler))]
          (is (success? response))
          (is (= "test" (-> response
                            :body
                            (json->)
                            :title)))))))

  (testing "provides swagger file at `/swagger.json`"
    (is (= 200
           (-> (mock/request :get "/swagger.json")
               (test-handler)
               :status))))

  (testing "serves static files at `/site`"
    (is (= 200
           (-> (mock/request :get "/site/index.html")
               (test-handler)
               :status)))))

(deftest env->config
  (testing "memory storage by default"
    (is (= :memory (:storage-type (sut/env->config {})))))

  (testing "converts `storage-type` to keyword"
    (is (= :file (-> {:storage-type "file"}
                     (sut/env->config)
                     :storage-type))))

  (testing "adds http port"
    (is (= 1234 (:port (sut/env->config {:port 1234})))))

  (testing "uses http port `8080` by default"
    (is (= 8080 (:port (sut/env->config {}))))))

(deftest -main
  (testing "starts http server"
    (with-redefs-fn {#'http/run-server (constantly :test-server)}
      #(is (= :test-server (sut/-main))))))
