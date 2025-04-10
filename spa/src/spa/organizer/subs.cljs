(ns spa.organizer.subs
  (:require
   [re-frame.core :as re-frame]
   [spa.subs :as base]))

(re-frame/reg-sub
 ::usernames
 :<- [::base/gui-state]
 (fn [state]
   (vals (:id->name state))))
