(ns upshot.handler.response
  (:require [clojure.spec.alpha :as s]))

(s/def ::result map?)

(s/def ::ok
  (s/cat ::status #{::ok}
         ::result ::result))

(s/def ::error
  (s/cat ::status #{::error}
         ::result ::result))

(defn- get-error-location
  [handler params]
  {:location {:handler handler
              :params params}})

(defn success-response
  [result]
  [::ok result])

(defn error-response
  ([message]
   [::error {:error {:message message}}])
  ([handler params result]
   [::error {:error (get-error-location handler params)}]))

(defn exception-response
  [handler params exc]
  [::error {:error (merge (get-error-location handler params)
                          {:message (.getMessage exc)
                           :exception exc})}])
