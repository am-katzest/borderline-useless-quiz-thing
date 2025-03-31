(ns buqt.model.question
  (:require #?(:clj [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])
            [buqt.model.utils :as u]))

;; interface
(defn- question-type [q & _] (:question-type q))

(defmulti initialize question-type)

(defmulti grade "(question, answer) -> points" question-type)

(defmulti invariants "returns keys which cannot be changed" question-type)

(defmulti validate question-type)
;; creation

(defn question [desc]
  (initialize
   {:description "..."
    :question-type (:type desc)
    :points 1
    :state :hidden}
   desc))

;; validation

(s/defschema base-question
  {:description s/Str
   :question-type s/Keyword
   :points (s/constrained s/Num (complement neg?))
   :state (s/enum :hidden :active :stopped :revealed)})

(defn update-valid? [before after]
  (let [invariants (conj (invariants before) :question-type)]
    (and (= (select-keys before invariants)
            (select-keys after invariants))
         (validate after))))
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

(defmethod invariants :abcd
  [_]
  [:count])

(s/defschema abcd-question
  (into base-question
        {:correct-answer (s/constrained s/Int (complement neg?))
         :count (s/constrained s/Int pos-int?)
         :possible-answers [s/Str]}))

(defmethod validate :abcd
  [question]
  (and (not (s/check abcd-question question))
       (= (:count question)
          (count (:possible-answers question)))
       (<= 0 (:correct-answer question) (:count question))))

