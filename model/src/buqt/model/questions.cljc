(ns buqt.model.questions
  (:require [buqt.model.question :as q]))

(defn next-question-id [questions]
  (->> questions keys (cons 0) (apply max) inc))

(defn update-question [questions id updated]
  (assoc questions id updated))

(defn update-valid? [questions id replacement]
  (let [existing (questions id)]
    (q/update-valid? existing replacement)))

(defn max-points [id->question]
  (->> id->question vals (keep :points) (reduce + 0)))

(defn- min-max+ [a b]
  (merge-with + a b))

(defn- up-to [x]
  {:min 0 :max x})

(defn- exactly [x]
  {:min x :max x})

(defn tally-points-for-answers-participant
  "returns a range in which total points may be"
  [id->question id->answer]
  (->> id->question
       (map (fn [[id question]]
              (let [answer (id->answer id)]
                (cond (not (and question answer))
                      (exactly 0)
                      ;; participant doesn't have this information anyway, so checking would crash
                      (q/participant-can-see-answers? (:state question))
                      (exactly (q/grade question answer))
                      :else
                      (up-to (:points question))))))
       (reduce min-max+ (exactly 0))))

(defn tally-points-for-answers-organizer
  "returns exact score"
  [id->question id->answer]
  (->> id->question
       (map (fn [[id question]]
              (let [answer (id->answer id)]
                (if (and question answer)
                  (q/grade question answer)
                  0))))
       (reduce + 0)))
