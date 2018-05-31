(ns upshot.core-test
  (:require [clojure.test :refer :all]
            [upshot.core :refer :all]
            [upshot.command.response :as command-response]))

(defn tx-data-handler
  [env params]
  {:success true
   :tx-data [{:test "hello"}]})

(deftest processor-tests
  (let [handlers {:tx-data #'tx-data-handler}
        p (processor handlers)
        cmd [::command-response/ok :tx-data [{:test "data"}]]
        result (p {} cmd)]
    ))
