(ns spa.participant.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.participant.subs :as ps]
   [spa.participant.events :as pe]))

(defn username-edit-panel []
  [re-com/h-box
   :children
   [[re-com/label :label "chaneg usernam!"]
    [re-com/input-text
     :model (sub ::ps/username)
     :change-on-blur? false
     :on-change #(evt [::pe/changed-username %])]
    ]])

(defn participant-panel []
  [username-edit-panel])
