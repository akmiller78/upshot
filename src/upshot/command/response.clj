(ns upshot.command.response
  (:require [clojure.spec.alpha :as s]))

(s/def ::effect (s/keys :req [::handler
                              ::params]))
(s/def ::ok
  (s/cat ::status #{::ok}
         ::effects
         (s/+ (s/cat ::handler keyword?
                     ::params (s/? vector?)))))

(s/def ::invalid
  (s/cat ::status #{::invalid}
         ::spec map?))
