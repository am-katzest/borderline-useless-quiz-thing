(ns spa.participant.subs 
    (:require
     [re-frame.core :as re-frame]
     [spa.subs :as base]))

(re-frame/reg-sub
 ::username
 :<- [::base/gui-state]
 (fn [state]
   (:username state)))
