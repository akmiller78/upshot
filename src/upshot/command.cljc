(ns upshot.command
  (:require [upshot.command.response :as response]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/def ::command-key keyword?)
(s/def ::command-params map?)

(s/def ::command (s/keys :req [::handler
                               ::params]))

(defmulti command
  (fn [k params]
    {:pre [(s/valid? ::command-key k)
           (s/valid? ::command-params params)]}
    k))


(comment

  (defmethod command :test
    [_ params]
    [:ok])

  (command :test {})

  (s/valid? ::command-params {})
  (s/explain ::command-params {})

  )
