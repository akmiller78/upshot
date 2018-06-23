(ns upshot.command
  (:require [upshot.command.response :as response]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(s/def ::command-key keyword?)
(s/def ::command-params map?)

(defmulti command
  (fn [k params]
    {:pre [(s/valid? ::command-key k)
           (s/valid? ::command-params params)]}
    k))
