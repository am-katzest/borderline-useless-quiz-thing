(ns spa.organizer.subs
  (:require
   [re-frame.core :as re-frame]
   [spa.subs :as base]))

(re-frame/reg-sub
 ::users+names
 :<- [::base/gui-state]
 (fn [state]
   (seq (:id->name state))))
