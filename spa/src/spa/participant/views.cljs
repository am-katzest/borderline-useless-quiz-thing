(ns spa.participant.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.participant.subs :as ps]
   [spa.shared-views :as shared-views]
   [spa.ui-elements :as els]
   [spa.styles :as styles]
   [buqt.model.question :as q]
   [spa.subs :as s]
   [spa.events :as e]
   [spa.participant.events :as pe]))

(defn username-edit-panel []
  (let [val-set ((els/make-val-set {:username (sub ::ps/username)}
                                   #(evt [::pe/changed-username (:username %)]))
                 [:username])]
    [els/fancy-input "username" val-set "200px"]))

(defn display-points-box [[a b] separator width]
  [re-com/h-box :class (styles/points-box)
         :gap "5px"
         :children
         [[re-com/label :label "points:"]
          [re-com/label :style {:min-width width :text-align "right"} :label (els/format-number-for-display a)]
          [re-com/label :label separator]
          [re-com/label :style {:min-width width} :label (els/format-number-for-display b)]]])

(defn points-box []
  [display-points-box
   [(or (sub ::ps/selected-question-points) "")
    (:points (sub ::s/selected-question))]
   "/"
   "1.5em"])



(defn question-state [state]
  (let [label (get {:active "answer question"
                    :visible "you can't answer yet"
                    :stopped "you can't answer anymore"
                    :revealed "answer revealed"}
                   state
                   "uh oh")]
    [re-com/label :label label]))

(defmulti question-edit (fn [question change-answer!] (:question-type question)))

(defmethod question-edit :abcd
  [question change-answer!]
  [re-com/v-box
   :children
   [[re-com/label :label "possible answers:"]
    [re-com/gap :size "10px"]
    (doall (for [[letter i] (map vector q/letters (range (:count question)))
                 :let [selected? (= i (sub ::ps/own-answer-to-selected-question))
                       correct?  (= i (:correct-answer question))
                       known?    (q/participant-can-see-answers? (:state question))
                       editable? (q/participant-can-change-answer? (:state question))]]
             ^{:key i}
             [re-com/h-box
              :align :center
              :padding "5px"
              :gap "20px"
              :children [[re-com/button
                          :class (styles/abcd-question-answer-btn selected? correct? known? editable?)
                          :label letter
                          :on-click #(change-answer! i)]
                         [re-com/label :label (get-in question [:possible-answers i])]]]))]])

(defmethod question-edit :bools
  [question change-answer!]
  [re-com/v-box
   :children
   [[re-com/label :label "true/false statements:"]
    [re-com/gap :size "10px"]
    (let [raw-answer (sub ::ps/own-answer-to-selected-question)
          answer (or  raw-answer (vec (repeat (:count question) false)))
           ; ugly hack, to fix inital "nil" answer displaying as plausible answer to participant
          _ (when (nil? raw-answer) (change-answer! answer))
          known?    (q/participant-can-see-answers? (:state question))
          editable? (q/participant-can-change-answer? (:state question))]
      (doall (for [i (range (:count question))
                   :let [val (get answer i)
                         correct?  (= (get-in question [:key i]) val)]]
               ^{:key i}
               [re-com/h-box
                :align :center
                :padding "5px"
                :gap "20px"
                :children [[re-com/button
                            :class (styles/bool-edit-btn val known? editable? correct?)
                            :label (if val "T" "F")
                            :disabled? (not editable?)
                            :on-click #(change-answer! (update answer i not))]
                           [re-com/label :label (get-in question [:descriptions i])]]])))]])

(defn display-order-plain [question positions]
  [re-com/v-box
   :children
   (doall (for [relative positions]
            [re-com/h-box
             :padding "5px"
             :align :center
             :children [[re-com/md-icon-button :md-icon-name "zdmi-blank" :disabled? true]
                        [els/fancy-input "" (els/->val-set question [:descriptions relative]) "400px" :disabled? true]]]))])

(defmethod question-edit :order
  [question change-answer!]
  [re-com/v-box
   :children
   [[re-com/label :label "your answer:"]
    [re-com/gap :size "10px"]
    (let [raw-answer (sub ::ps/own-answer-to-selected-question)
          answer (or  raw-answer (vec (range (:count question))))
          _ (when (nil? raw-answer) (change-answer! answer))
          known?    (q/participant-can-see-answers? (:state question))
          editable? (q/participant-can-change-answer? (:state question))]
      (if
          editable?
          [shared-views/reorder-list question answer change-answer!]
          [:<>
           (display-order-plain question answer)
           (when known?
             [:<>
              [re-com/gap :size "20px"]
              [re-com/label :label "correct answer:"]
              [re-com/gap :size "10px"]
              (display-order-plain question (:correct-order question))])]))]])

(defmethod question-edit :text
  [question change-answer!]
  [re-com/v-box
   :padding "10px"
   :children [[els/fancy-input
               "your answer:"
               [(sub ::ps/own-answer-to-selected-question) change-answer!]
               "400px"
               :disabled? (not (q/participant-can-change-answer? (:state question)))]]])

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


(defn total-points-box []
  (let [{min-points :min  max-points :max} (sub ::ps/points)]
    (if (not= min-points max-points)
      [display-points-box [min-points max-points] "-" "2.5em"]
      [display-points-box [min-points nil] "" "2.5em"])))

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
                        (do
                          (evt ::e/goto-question-with-highest-number)
                          "question not selected"))]
              [re-com/v-box
               :size "1"
               :class (styles/participant-right-panel)
               :children [[total-points-box]
                          [username-edit-panel]]]]])
