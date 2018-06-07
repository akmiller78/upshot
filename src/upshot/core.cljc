(ns upshot.core
  (:require [clojure.spec.alpha :as s]
            [upshot.response :as response]
            [upshot.command :as command]
            [upshot.command.response :as command-response]
            [upshot.handler.response :as handler-response]))

(s/def ::handlers (s/map-of keyword? var?))

(defn- apply-fx!
  "Execute side-effecting functions specified in fx"
  [env handlers fx]
  {:pre [(s/valid? (s/coll-of ::command-response/effect) fx)]
   :post [(or (s/valid? ::handler-response/ok %)
              (s/valid? ::handler-response/error %))]}
  (reduce (fn [[_ last-result] {::command-response/keys [handler params]}]
            (if-let [handler-fn (handler handlers)]
              (try
                (let [[status handler-result] (apply handler-fn env params)
                      result (merge last-result
                                    {handler handler-result})
                      successful? (= status ::handler-response/ok)]
                  (if successful?
                    (handler-response/success-response result)
                    (reduced
                     (handler-response/error-response handler params result))))
                (catch Exception e
                  (reduced
                   (handler-response/exception-response handler params e))))
              (reduced
               (handler-response/error-response
                (str "Missing handler for specified key: " handler)))))
          [] fx))

(defn processor
  "Generate a command response processor function given the
  environment context and a map of keys to handler functions
  for processing the results of a given command. The execution
  of the returned fn is NOT idempotent."
  [handlers]
  {:pre [(s/valid? ::handlers handlers)]
   :post [(fn? %)]}
  (fn [env command-response]
    {:pre [(or (nil? env) (map? env))
           (or (s/valid? ::command-response/ok command-response)
               (s/valid? ::command-response/invalid command-response))]
     :post [(or (s/valid? ::response/ok %)
                (s/valid? ::response/invalid %)
                (s/valid? ::response/error %))]}
    (let [[status & _] command-response]
      (condp = status
        ::command-response/invalid [::response/invalid (last command-response)]
        ::command-response/ok (let [{::command-response/keys [status effects]}
                                    (s/conform ::command-response/ok command-response)
                                    [status m] (apply-fx! env
                                                          handlers
                                                          effects)]
                                (condp = status
                                  ::handler-response/ok
                                  [::response/ok m]
                                  ::handler-response/error
                                  [::response/error m]))))))


(comment

  (defn tx-data-h [env params]
    [::handler-response/ok
     {:name "Adam"}])

  (defn send-email-h [env params]
    [::handler-response/ok
     {:subject "Hello"}])

  (def handlers {:tx-data #'tx-data-h
                 :send-email #'send-email-h})

  (s/valid? ::handlers handlers)

  (def p (processor handlers))

  (def cmd [::command-response/ok :tx-data [{:name "Adam"}] :send-email [{:subject "hi"}]])
  (def invalid-cmd [::command-response/invalid {:spec-error true}])

  (def r
    (s/conform ::command-response/ok cmd))

  r

  (::command-response/effects r)
  (p {} invalid-cmd)
  (p {} cmd)

  )
