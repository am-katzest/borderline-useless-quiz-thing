(ns spa.participant.events
  (:require [spa.events :as e :refer [reg-input-event]]))

(reg-input-event
 ::changed-username
 (fn [new-username]
   {:type :input/change-username
    :username new-username}))
