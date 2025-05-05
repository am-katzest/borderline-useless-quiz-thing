(ns buqt.model.question
  (:require #?(:clj [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])
            [buqt.model.utils :as u]))

;; interface
(defn- question-type [q & _] (:question-type q))

(defmulti initialize question-type)

(defmulti grade "(question, answer) -> points" question-type)

(defmethod grade nil
  [_ _] 0)

(defmulti invariants "returns keys which cannot be changed" question-type)

(defmulti validate question-type)

(defmulti secrets "returns keys which must be kept from participants" question-type)

(defmulti validate-answer "is the answer format proper" question-type)
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
   :state (s/enum :hidden :visible :active :stopped :revealed)})

(defn update-valid? [before after]
  (let [invariants (conj (invariants before) :question-type)]
    (and (= (select-keys before invariants)
            (select-keys after invariants))
         (validate after))))
;; helpers

(def letters ["A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"])

(defn- all-or-zero [q b]
  (if b (:points q) 0))

;; keeping information from clients

(defn- participant-can-see-question? [state]
  (not= state :hidden))

(defn participant-can-see-answers? [state]
  (= state :revealed))

(defn- participant-can-change-answer? [state]
  (= state :active))

(defn- remove-keys [m c]
  (reduce dissoc m c))

(defn censor [question]
  (when question
    (let [state (:state question)]
      (cond (participant-can-see-answers? state) question
            (participant-can-see-question? state) (remove-keys question (secrets question))
            :else nil))))

;; answers

(defn can-change-answer? [question answer]
  (and question
       (participant-can-change-answer? (:state question))
       (or (nil? answer) (validate-answer question answer))))

;; abcd
(defmethod initialize :abcd
  [base {:keys [count]}]
  (u/assert* (<= 2 count 25))
  (let [answers (vec (repeat count ""))]
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
         :possible-answers (s/constrained [s/Str] vector?)}))

(defmethod validate :abcd
  [question]
  (and (not (s/check abcd-question question))
       (= (:count question)
          (count (:possible-answers question)))
       (<= 0 (:correct-answer question) (:count question))))

(defmethod secrets :abcd
  [_]
  [:correct-answer])

(defmethod validate-answer :abcd
  [{:keys [count]} answer]
  (and (int? answer) (<= 0 answer (dec count))))

;; text
(defmethod initialize :text
  [base _]
  (assoc base
         :answer->points {}))

(defmethod grade :text
  [question answer]
  (get-in question [:answer->points answer] 0))

(defmethod invariants :text
  [_]
  [])

(s/defschema text-question
  (into base-question
        {:answer->points {s/Str (s/constrained s/Num (complement neg?))}}))

(defmethod validate :text
  [{:keys [points] :as question}]
  (and  (not (s/check text-question question))
        (every? #(>= points %) (vals (:answer->points question)))))

(defmethod secrets :text
  [_]
  [:answer->points])

(defmethod validate-answer :text
  [_ answer]
  (string? answer))

;; bools
(defmethod initialize :bools
  [base {:keys [count]}]
  (u/assert* (<= 1 count))
  (let [descriptions (vec (repeat count ""))
        key (vec (repeat count true))]
    (assoc base
           :count count
           :descriptions descriptions
           :key key)))

(defmethod grade :bools
  [question answer]
  (let [correct (count (filter true? (map = answer (:key question))))]
    (* (:points question) (/ correct (:count question)))))

(defmethod invariants :bools
  [_]
  [:count])

(s/defschema bools-question
  (into base-question
        {:count (s/constrained s/Int pos-int?)
         :key (s/constrained [s/Bool] vector?)
         :descriptions (s/constrained [s/Str] vector?)}))

(defmethod validate :bools
  [question]
  (and (not (s/check bools-question question))
       (= (:count question)
          (count (:descriptions question))
          (count (:key question)))))

(defmethod secrets :bools
  [_]
  [:key])

(defmethod validate-answer :bools
  [question answer]
  (= (count answer) (:count question)))

;; order
(defmethod initialize :order
  [base {:keys [count]}]
  (u/assert* (<= 1 count))
  (let [descriptions (vec (repeat count ""))
        key (vec (range count))]
    (assoc base
           :count count
           :descriptions descriptions
           :correct-order key
           :grade-method :neighboring-pairs)))

(defmulti grade-order (fn [method _count _order] method))

(defmethod grade-order :neighboring-pairs
  [_ q-count order]
  (let [fraction-per-correct-pair (/ (dec q-count))]
    (->> order
         (partition 2 1)
         (filter (fn [[before after]] (= before (dec after))))
         count
         (* fraction-per-correct-pair))))

(defmethod grade-order :in-place
  [_ q-count order]
  (let [fraction-per-answer-in-place (/ q-count)]
    (->> order
         (map-indexed vector)
         (filter (fn [[place element]] (= place element)))
         count
         (* fraction-per-answer-in-place))))

(defn invert-order
  "^-1 the #(reorder-vec <order> %)"
  [order]
  (loop [acc (vec (repeat (count order) nil))
         [[i x] & rest] (map-indexed vector order)]
    (if-not i
      acc 
      (recur (assoc acc x i)
             rest))))

(defn reorder-vec [order xs]
  (mapv xs order))

(defmethod grade :order
  [{:keys [grade-method count correct-order points]} answer]
  (double
   (* points
      (grade-order grade-method count (reorder-vec
                                       (invert-order correct-order)
                                       answer)))))

(defmethod invariants :order
  [_]
  [:count])

(defn- correct-order? [ints]
  (= (into #{} ints)
     (into #{} (range (count ints)))))

(s/defschema order-question
  (into base-question
        {:count (s/constrained s/Int #(>= % 2))
         :correct-order (-> [s/Int]
                            (s/constrained vector?)
                            (s/constrained correct-order?))
         :descriptions (s/constrained [s/Str] vector?)
         :grade-method (s/constrained s/Keyword #(contains? #{:neighboring-pairs :in-place} %))}))

(defmethod validate :order
  [question]
  (and (not (s/check order-question question))
       (= (:count question)
          (count (:descriptions question))
          (count (:correct-order question)))))

(defmethod secrets :order
  [_]
  [:correct-order])

(defmethod validate-answer :order
  [question answer]
  (and (= (count answer) (:count question))
       (correct-order? answer)))
