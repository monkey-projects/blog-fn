(ns monkey.blog.fe.test.fixtures
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]))

;; Fixtures are different between clj and cljs
#?(:cljs
   (do
     (defn restore-re-frame []
       (let [restore-point (atom nil)]
         {:before #(reset! restore-point (rf/make-restore-fn))
          :after  #(@restore-point)}))

     (def reset-db {:before #(reset! app-db {})}))

   :clj
   (do
     (defn restore-re-frame []
       (fn [f]
         (let [rp (rf/make-restore-fn)]
           (try
             (f)
             (finally
               (rp))))))

     (defn reset-db [f]
       (reset! app-db {})
       (f))))

