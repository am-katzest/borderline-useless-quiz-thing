(ns buqt.model.questions
  (:require [buqt.model.question :as q]))

(defn next-question-id [questions]
  (->> questions keys (cons 0) (apply max) inc))

(defn update-question [questions id updated]
  (assoc questions id updated))

(defn update-valid? [questions id replacement]
  (let [existing (questions id)]
    (q/update-valid? existing replacement)))
