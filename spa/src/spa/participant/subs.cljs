(ns spa.participant.subs 
    (:require
     [re-frame.core :as re-frame]
     [buqt.model.question :as q]
     [buqt.model.questions :as qs]
     [spa.subs :as base]))

(re-frame/reg-sub
 ::username
 :<- [::base/gui-state]
 (fn [state]
   (:username state)))

(re-frame/reg-sub
 ::own-answer-to-selected-question
 :<- [::base/gui-state]
 :<- [::base/selected-question-id]
 (fn [[state question-id]]
   (get-in state [:question->answer question-id])))

(re-frame/reg-sub
 ::selected-question-points
 :<- [::base/selected-question]
 :<- [::own-answer-to-selected-question]
 (fn [[question answer]]
   (when (q/participant-can-see-answers?
          (:state question))
     (q/grade question answer))))

(re-frame/reg-sub
 ::points
 :<- [::base/gui-state]
 (fn [state]
   (qs/tally-points-for-answers-participant
    (:questions state)
    (:question->answer state))))
