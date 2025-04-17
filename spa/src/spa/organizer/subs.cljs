(ns spa.organizer.subs
  (:require
   [re-frame.core :as re-frame]
   [buqt.model.questions :as qs]
   [spa.subs :as base]))

(re-frame/reg-sub
 ::users+names
 :<- [::base/gui-state]
 (fn [state]
   (seq (:id->name state))))

(re-frame/reg-sub
 ::participant->answers
 :<- [::base/gui-state]
 :-> :participant->question->answer)

(re-frame/reg-sub
 ::participant-answers
 :<- [::participant->answers]
 (fn [participant->answers [_ id]]
   (participant->answers id)))

(re-frame/reg-sub
 ::participant-points
 (fn [[_ id]]
   [(re-frame/subscribe [::base/id->question])
    (re-frame/subscribe [::participant-answers id])])
 (fn [[id->question answers]]
   (qs/tally-points-for-answers-organizer id->question answers)))

(re-frame/reg-sub
 ::max-points
 :<- [::base/id->question]
 (fn [id->question]
   (qs/max-points id->question)))

(re-frame/reg-sub
 ::participant-answer-for-selected-question
 (fn [[_ id]]
   [(re-frame/subscribe [::base/selected-question-id])
    (re-frame/subscribe [::participant-answers id])])
 (fn [[question-id question-id->answer]]
   (question-id->answer question-id)))
