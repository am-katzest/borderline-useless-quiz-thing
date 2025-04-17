(ns spa.participant.events
  (:require [spa.events :as e :refer [reg-input-event]]))

(reg-input-event
 ::changed-username
 (fn [new-username]
   {:type :input/change-username
    :username new-username}))

(reg-input-event
 ::change-answer
 (fn [question-id answer]
   {:type :input/change-answer
    :question-id question-id
    :answer answer}))
