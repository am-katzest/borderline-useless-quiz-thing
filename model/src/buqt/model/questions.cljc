(ns buqt.model.questions
  (:require [buqt.model.question :as q]))

(defn- next-question-id [state]
  (->> state keys (cons 0) (apply max) inc))

(defn add-question [questions desc]
  (let [id (next-question-id questions)
        q (q/question desc)]
    (assoc questions id q)))
