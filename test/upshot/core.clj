(ns upshot.core-test
  (:require [clojure.test :refer :all]
            [upshot.core :refer :all]
            [upshot.response :as response]
            [upshot.handler.response :as handler-response]
            [upshot.command.response :as command-response]))

(defn tx-data-handler
  [env params]
  [::handler-response/ok {:tx-data [{:tempids [1]}]}])

(deftest processor-tests
  (testing "Valid commands with existing command handlers"
    (let [handlers {:tx-data #'tx-data-handler}
          p (processor handlers)
          cmd [::command-response/ok :tx-data [{:test "data"}]]
          result (p {} cmd)]
      (is (vector? result))
      (is (= ::response/ok (first result)))
      (is (contains? (last result) :tx-data)))))


(comment

  (def cmd1 [::command-response/ok :tx-data [{:test "data"}]])

  (def p1 (processor {:tx-data #'tx-data-handler}))

  (p1 {} cmd1)
  )
