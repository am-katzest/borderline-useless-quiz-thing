(ns buqt.model.question
  (:require [buqt.model.utils :as u]))

;; interface
(defn- question-type [q & _] (:question-type q))

(defmulti initialize question-type)

(defmulti grade "(question, answer) -> points" question-type)

(defmulti validate question-type)
;; creation

(defn question [desc]
  (initialize
   {:description "..."
    :question-type (:type desc)
    :points 1
    :state :hidden}
   desc))
;; helpers

(def ^:private letters
  (for [i (range 26)]
    (str (char (+ (int \A) i)))))

(defn- all-or-zero [q b]
  (if b (:points q) 0))

;; abcd
(defmethod initialize :abcd
  [base {:keys [count]}]
  (u/assert* (<= 2 count 25))
  (let [answers (vec (take count letters))]
    (assoc base
           :count count
           :possible-answers answers
           :correct-answer 0)))

(defmethod grade :abcd
  [question answer]
  (all-or-zero question (= (:correct-answer question) answer)))

(defmethod validate :abcd
  [question]
  (and (= (:count question)
          (count (:possible-answers question)))
       (<= 0 (:correct-answer question) (:count question))))
