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
 ::user-points
 :<- [::base/gui-state]
 (fn [state [_ id]]
   (qs/tally-points-for-answers-organizer
    (:questions state)
    (get-in state [:participant->question->answer id]))))
