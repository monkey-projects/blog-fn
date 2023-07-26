(ns monkey.blog.fe.test.junit-reporter)

(defn printerr [msg & args]
  (binding [*print-fn* *print-err-fn*]
    (apply println msg args)))

(defmulti update-report :type)

(defmethod update-report :default [ctx]
  ;; Noop
  (printerr "Unsupported test tag type:" (:type ctx))
  (printerr "All:" ctx)
  ctx)

(defn- add-out [ctx msg]
  (assoc ctx ::out msg))

(defn- update-state [ctx f & args]
  (apply update ctx ::state f args))

(defmethod update-report :begin-run-tests [ctx]
  (add-out ctx "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testsuites>"))

(defmethod update-report :end-run-tests [ctx]
  (add-out ctx "</testsuites>"))

(defmethod update-report :begin-test-ns [ctx]
  (-> ctx
      (add-out "<testsuite>")
      (update-state assoc :ns (:ns ctx))))

(defmethod update-report :end-test-ns [ctx]
  (-> ctx
      (add-out "</testsuite>")
      (update-state dissoc :ns)))

(defmethod update-report :begin-test-var [{:keys [var] :as ctx}]
  (printerr "Begin var:" var)
  (assoc-in ctx [::state :var] var))

(defmethod update-report :end-test-var [{:keys [var] :as ctx}]
  (printerr "End var:" var)
  #_(update-in ctx [::state :var] conj var)
  (update-state ctx dissoc :var))

(defmethod update-report :pass [ctx]
  (update-state ctx update :pass (fnil inc 0)))

(defmethod update-report :fail [ctx]
  (update-state ctx update :fail (fnil inc 0)))

(defmethod update-report :error [ctx]
  (update-state ctx update :error (fnil inc 0)))

(defmethod update-report :summary [{:keys [pass fail error test] :as ctx}]
  (println "Test executed:" test "total," pass "passed," fail "failed," error "error(s)")
  ctx)

(defn reporter [state {:keys [type] :as ctx}]
  #_(printerr "Type:" type ", keys:" (keys ctx))
  ;; Not for parallel execution, but that's ok in node
  (let [{:keys [::out] new-state ::state :as new-ctx} (update-report (assoc ctx ::state @state))]
    (reset! state new-state)
    ;; Print output, if any
    (when out
      (println out))))
