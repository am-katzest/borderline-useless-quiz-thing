(ns spa.participant.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.participant.subs :as ps]
   [spa.shared-views :as shared-views]
   [spa.ui-elements :as els]
   [spa.styles :as styles]
   [spa.participant.events :as pe]))

(defn username-edit-panel []
  (let [val-set ((els/make-val-set {:username (sub ::ps/username)}
                                   #(evt [::pe/changed-username (:username %)]))
                 [:username])]
    [els/fancy-input "username" val-set "200px"]))

(defn points-box []
  [re-com/h-box :class (styles/points-box)
         :gap "5px"
         :children
         [[re-com/label :label "points:"]
          [re-com/label :style {:min-width "1.5em" :text-align "right"} :label (or (sub ::ps/selected-question-points) "")]
          [re-com/label :label "/"]
          [re-com/label :style {:min-width "1.5em"} :label (:points (sub ::s/selected-question))]]])
(defn participant-panel []
  [re-com/h-box
   :class (styles/participant-panel)
   :children [[re-com/v-box
               :size "1"
               :class (styles/questions-box)
               :children [[shared-views/questions-list]]]
              [re-com/box
               :size "4"
               :child "meow" ]
              [re-com/v-box
               :size "1"
               :class (styles/participant-right-panel)
               :children [[username-edit-panel]]]]])
