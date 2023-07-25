(ns monkey.blog.fe.test.junit-runner
  (:require [cljs.test :as ct]
            [shadow.test :as st]
            [shadow.test.node :as stn]))

(defn printerr [msg & args]
  (binding [*print-fn* *print-err-fn*]
    (apply println msg args)))

(defmulti update-report :type)

(defmethod update-report :default [ctx]
  ;; Noop
  ctx)

(defn- add-out [ctx msg]
  (assoc ctx ::out msg))

(defmethod update-report :begin-run-tests [ctx]
  (add-out ctx "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testsuites>"))

(defmethod update-report :end-run-tests [ctx]
  (add-out ctx "</testsuites>"))

(defmethod update-report :begin-test-ns [ctx]
  (add-out ctx "<testsuite>"))

(defmethod update-report :end-test-ns [ctx]
  (add-out ctx "</testsuite>"))

(defn reporter [state {:keys [type] :as ctx}]
  (printerr "Type:" type ", keys:" (keys ctx))
  ;; Not for parallel execution, but that's ok in node
  (let [{:keys [::out] new-state ::state :as new-ctx} (update-report (assoc ctx ::state @state))]
    (reset! state new-state)
    ;; Print output, if any
    (when out
      (println out))))

(defn run-tests [& args]
  (stn/reset-test-data!)
  (let [state (atom nil)]
    (st/run-all-tests (-> (ct/empty-env)
                          (assoc :report-fn (partial reporter state)))
                      nil)))
