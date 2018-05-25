(ns upshot.core
  (:require [clojure.spec.alpha :as s]
            [upshot.core.command :as command]
            [upshot.command.response :as command-response]))

(s/def ::handlers (s/map-of keyword? var?))

(defn- apply-handlers!
  "Execute side-effecting functions specified in fx"
  [env handlers fx]
  {:pre [(s/valid? (s/coll-of ::command/command) fx)]}
  (reduce (fn [result {::keys [handler params]}]
            (if-let [handler-fn (handler handlers)]
              (try
                (let [{:as handler-result
                       :keys [success]}
                      (apply handler-fn env params)]
                  (if success
                    (merge result handler-result)
                    (reduced
                     (merge result
                            {:success false
                             :error {:location {:handler handler
                                                :params params}}}))))
               e (catch Exception e
                  (reduced {:success false
                            :error {:location {:handler handler
                                               :params params}
                                    :message (.getMessage e)
                                    :exception e}})))
              (reduced
               (merge result
                      {:success false
                       :error {:message
                               (str "Missing handler for specified key: " handler)}}))))
          {} fx))

(defn processor
  "Generate a command response processor function given the
  environment context and a map of keys to handler functions
  for processing the results of a given command. The execution
  of the returned fn is NOT idempotent."
  [handlers]
  {:pre [(s/valid? ::handlers handlers)]}
  (fn [env command-response]
    {:pre [(or (nil? env) (map? env))
           (or (s/valid? ::command-response/ok command-response)
               (s/valid? ::command-response/invalid command-response))]}
    (let [[status & _] command-response]
      (condp = status
        ::invalid command-response
        ::ok (let [{::keys [status effect]}
                   (s/conform ::ok-response command-response)
                   _ (println effect)]
               (apply-handlers env
                               handlers
                               effect))))))
