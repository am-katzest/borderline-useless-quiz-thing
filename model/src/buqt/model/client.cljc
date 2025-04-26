(ns buqt.model.client
  (:require [buqt.model.utils :refer [assert*] :as u]
            [buqt.model.questions :as qs]
            [buqt.model.impact :as i]
            [buqt.model.question :as q]))


;; doesn't concern itself with `cnt`, that's handled elsewhere™

(defn make-participant [id]
  {:user-type :participant
   :id id
   :cnt 0
   :questions {}
   :question->answer {}
   :username ""})

(defn make-organizer [id]
  {:user-type :organizer
   :id id
   :cnt 0
   :questions {}
   :participant->question->answer {}
   :id->name {}})


(def ^:private user-hierarchy
  (-> (make-hierarchy)
      (derive :participant :both)
      (derive :organizer :both)
      (derive :both :any)
      (derive :none :any)))

(defmulti apply-update (fn [state msg] [(or (#{:organizer :participant} (:user-type state)) :none) (:type msg)])
  :hierarchy #'user-hierarchy)

(defmethod apply-update [:participant :update/change-username]
  [state {:keys [username]}]
  (assoc state :username username))

(defmethod apply-update [:organizer :update/change-username]
  [state {:keys [username id]}]
  (update state :id->name assoc id username))

(defmethod apply-update [:organizer :update/add-participant]
  [state {:keys [id]}]
  (-> state
      (update :id->name assoc id "")
      (update :participant->question->answer assoc id {})))

(defmethod apply-update [:any :update/reset]
  [_state {:keys [state]}]
  state)

(defmethod i/update->compatibility
  :default [_ _] {:domain :global :impact :self-contained})

(defmethod i/update->compatibility
  :update/change-answer
  [_ {:keys [question-id]}]
  {:domain [:question question-id] :impact 1})

(defmethod i/update->compatibility
  :update/change-question
  [{:keys [questions]} {:keys [id question]}]
  {:domain [:question id]
   :impact (let [existing-question (questions id)
                 state (:state existing-question)
                 state' (:state question)]
             (cond (and state (not state')) :final  ;question deleted or hidden from participant
                   (= state state') :self-contained ;nothing important edited
                   (not= state state') 2            ;state changed, so it's safe to assume answer changes won't come through
                   ))})

(defn increment-cnt [m]
  (update m :cnt inc))

(defn cnt-matches? [state update]
  (= (:cnt state) (:cnt update)))

(defn taking-care-of-cnt [f]
  (fn [state update]
    (if (= :update/reset (:type update))
      (f state update)
      (if (cnt-matches? state update)
        (increment-cnt (f state update))
        ::cnt-mismatch))))

(defmulti input->action (fn [_s input] (:type input)))
(defmulti action->expected-update (fn [_s action] (:type action)))

(defmethod input->action :input/change-username [state input]
  (u/participant* state)
  {:type :action/change-username
   :username (:username input)})

(defmethod action->expected-update :action/change-username [state action]
  {:type :update/change-username
   :username (:username action)})

(defmethod input->action :input/add-question [state input]
  (u/organizer* state)
  {:type :action/add-question
   :desc (:desc input)})

(defmethod input->action :input/remove-question [state {:keys [question-id]}]
  (u/organizer* state)
  (u/assert* (get-in state [:questions question-id]) "no such question") 
  {:type :action/remove-question
   :question-id question-id})

(defmethod input->action :input/update-question [state {id :question-id question' :question}]
  (u/organizer* state)
  (let [question (get-in state [:questions id])]
    (u/assert* question "no question with this id")
    (u/assert* (q/update-valid? question question') "update invalid")
    {:type :action/update-question
     :question-id id
     :question question'}))

(defmethod input->action :input/change-answer [state {id :question-id answer :answer}]
  (u/participant* state)
  (let [question (get-in state [:questions id])]
    (u/assert* question "no question with this id")
    (u/assert* (q/can-change-answer? question answer))
    {:type :action/change-answer
     :question-id id
     :answer answer}))

(defmethod action->expected-update :action/add-question [state {:keys [desc]}]
  (u/organizer* state)
  {:type :update/change-question
   :id (qs/next-question-id (:questions state))
   :question (q/question desc)})

(defmethod action->expected-update :action/remove-question [state {:keys [question-id]}]
  (u/organizer* state)
  {:type :update/change-question
   :id question-id
   :question nil})

(defmethod action->expected-update :action/update-question [state {:keys [question-id question]}]
  (u/organizer* state)
  {:type :update/change-question
   :id question-id
   :question question})

(defmethod action->expected-update :action/change-answer [state {:keys [question-id answer]}]
  (u/participant* state)
  {:type :update/change-answer
   :participant-id (:id state)
   :question-id question-id
   :answer answer})

(defmethod apply-update [:both :update/change-question]
  [state {:keys [id question]}]
  (update state :questions qs/update-question id question))

(defmethod apply-update [:organizer :update/change-answer]
  [state {:keys [participant-id question-id answer]}]
  (assoc-in state [:participant->question->answer participant-id question-id] answer))

(defmethod apply-update [:participant :update/change-answer]
  [state {:keys [participant-id question-id answer]}]
  (u/assert* (= (:id state) participant-id) "update at wrong participant")
  (assoc-in state [:question->answer question-id] answer))
