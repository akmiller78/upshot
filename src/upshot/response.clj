(ns upshot.response
  (:require [clojure.spec.alpha :as s]))

(s/def ::result map?)

(s/def ::ok
  (s/cat ::status #{::ok}
         ::result ::result))

(s/def ::invalid
  (s/cat ::status #{::invalid}
         ::result ::result))

(s/def ::error
  (s/cat ::status #{::error}
         ::result ::result))
