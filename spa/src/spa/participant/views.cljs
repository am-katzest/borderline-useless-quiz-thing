(ns spa.participant.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.participant.subs :as ps]
   [spa.shared-views :as shared-views]
   [spa.ui-elements :as els]
   [spa.styles :as styles]
   [spa.subs :as s]
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

(defn question-state [state]
  (let [label (get {:active "answer question"
                    :visible "you can't answer yet"
                    :stopped "you can't answer anymore"
                    :revealed "answer revealed"}
                   state
                   "uh oh")]
    [re-com/label :label label]))

(defmulti question-edit (fn [question change-answer!] (:question-type question)))

(defn question-panel []
  (let [question (sub ::s/selected-question)
        question-id (sub ::s/selected-question-id)]
    [re-com/v-box
     :class (styles/question-participant)
     :gap "10px"
     :children
     [[re-com/label
       :class (styles/question-description)
       :label (:description question)]
      [re-com/h-box
       :gap "20px"
       :align :center
       :children
       [[points-box]
        [question-state (:state question)]]]
      [question-edit question #(evt [::pe/change-answer question-id %])]]]))


(defn participant-panel []
  [re-com/h-box
   :class (styles/participant-panel)
   :children [[re-com/v-box
               :size "1"
               :class (styles/questions-box)
               :children [[shared-views/questions-list]]]
              [re-com/box
               :size "4"
               :child (if (sub ::s/selected-question)
                        [question-panel]
                        "question not selected")]
              [re-com/v-box
               :size "1"
               :class (styles/participant-right-panel)
               :children [[username-edit-panel]]]]])
