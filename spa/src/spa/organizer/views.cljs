(ns spa.organizer.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.organizer.subs :as os]
   [reagent.core :as r]
   [spa.subs :as s]
   [spa.styles :as style]
   [spa.shared-views :as shared-views]
   [spa.ui-elements :as els]
   [spa.organizer.events :as oe]))

(defn url []
  [re-com/v-box
   :class (style/organizer-url)
   :children
   [[re-com/label :label [:p "here's a link you can copy: "]]
    [:tt {:style {:font-size :small}} (sub ::s/link)]]])

(defn user-list []
  [re-com/v-box
   :class (style/organizer-users-box)
   :size "auto"
   :children
   [[re-com/label :label (str "max points: " (sub ::os/max-points))]
    [re-com/box :child [re-com/label :label "users:"]]
    (doall (for [[id u] (sub ::os/users+names)]
       ^{:key id}
       [re-com/h-box
        :class (style/organizer-users-box-user)
        :children
        [[re-com/label
          :width "3em"
          :label (sub [::os/participant-points id])]
         [re-com/label
          :label (if (and u (not= "" u)) u "[empty]")]]]))]])

(defn fancy-input [label [val set] width & {:keys [blur?] :or {blur? false}}]
  [re-com/v-box
   :width width
   :children [[re-com/label :label label]
              [re-com/input-text
               :class (style/fancy-input)
               :style {:width width}
               :model val
               :change-on-blur? blur?
               :on-change set]]])


(defn question-state-edit [[value set]]
  [re-com/v-box
   :children [[re-com/label :label "set question state"]
              [re-com/horizontal-pill-tabs
               :model value
               :on-change set
               :tabs [{:id :hidden :label "hidden"}
                      {:id :visible :label "visible"}
                      {:id :active :label "active"}
                      {:id :stopped :label "stopped"}
                      {:id :revealed :label "revealed"}]]]])

(defn make-val-set [body action]
  (fn [path & {:keys [validate coerce display]
              :or {display identity
                   coerce identity
                   validate (constantly true)}}]
    [(display (get-in body path))
     (fn [val]
       (let [coerced (coerce val)]
         (when (validate coerced)
           (action (assoc-in body path coerced)))))]))

(defmulti edit-question-type-specific (fn [question val-set]
                                         (:question-type question)))

(def letters ["A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"])

(defmethod edit-question-type-specific
  :abcd
  [question val-set]
  [:<>
   [re-com/label :label "possible answers:"]
   [re-com/gap :size "10px"]
   (for [[letter i] (map vector letters (range (:count question)))]
     [re-com/h-box
      :align :center
      :padding "5px"
      :gap "20px"
      :children [[re-com/button
                  :class (style/abcd-question-btn (= i (:correct-answer question)))
                  :label letter
                  :on-click #((second (val-set [:correct-answer])) i)]
                 [fancy-input "" (val-set [:possible-answers i]) "400px" ]]])
   [re-com/gap :size "10px"]
   [re-com/label :label "participant answers:"]
   [re-com/gap :size "10px"]
   (doall (for [[id username] (sub ::os/users+names)
                :let [answer (sub [::os/participant-answer-for-selected-question id])]]
            [re-com/h-box :class (style/organizer-users-box-user) :gap "5px" :children [username  ":" (letters answer)]]))])

(defn question-edit []
  (let [question (sub ::s/selected-question)
        id (sub ::s/selected-question-id)
        val-set (make-val-set question #(evt [::oe/question-updated id %]))]
    [re-com/v-box
     :gap "20px"
     :class (style/question-edit)
     :children [[re-com/h-box
                 :justify :between
                 :children [[re-com/label :label "edit question!"]
                            [re-com/button
                             :style {:background-color style/clr-danger-a10
                                     :border :none
                                     :color :white}
                             :label "delete" :on-click #(evt [::oe/clicked-delete-question id])]]]
                [re-com/h-box
                 :width "100%"
                 :gap "20px"
                 :align :end
                 :children
                 [[fancy-input "description" (val-set [:description]) "400px"]
                  [fancy-input "points" (val-set [:points]
                                                 :display str
                                                 :coerce parse-double
                                                 :validate #(not (neg? %))
                                                 ) "50px" :blur? true]]]
                [question-state-edit (val-set [:state])]
                [edit-question-type-specific question val-set]]]))

(defn display-question []
  (if (sub ::s/selected-question)
    [question-edit]
    "no question selected"))

(defmulti initial-question-edit (fn [type _] type))
(defmulti initial-question-type-state identity)

(defmethod initial-question-type-state :abcd []
  {:count 4})

(defmethod initial-question-edit :abcd [_type desc]
  [re-com/v-box
   :children
   [[re-com/label :label "number of answers:"]
    [els/+-number-edit desc [:count] #(<= 2 % 20)]]])

(defn add-question []
  (let [question-type (r/atom :abcd)
        question-state (r/atom (initial-question-type-state :abcd))]
    [(fn []
       [re-com/h-box
        :class (style/add-question-box)
        :children [[re-com/v-box
                    :gap "20px"
                    :children
                    [[re-com/label :label "select question type:"]
                     [re-com/vertical-pill-tabs
                      :model question-type
                      :on-change (fn [type]
                                   (reset! question-type type)
                                   (reset! question-state (initial-question-type-state type)))
                      :tabs [{:id :abcd :label "abcd"}]]
                     [re-com/button :label "add question!" :on-click #(evt [::oe/add-question @question-type @question-state])]]]
                   [re-com/box :class (style/initial-question-edit-box) :child [initial-question-edit @question-type question-state]]]])]))


(defn questions-header []
  [re-com/h-box
   :class (style/questions-header)
   :justify :between
   :align :center
   :children [[re-com/label :style {:width "20px"} :label "questions:"]
              [re-com/button :style {:background-color style/clr-primary-a40
                                     :border :none
                                     :color :white}
               :label "add new"
               :on-click #(evt ::oe/show-add-question-ui)]]])

(defn questions-box []
  [re-com/v-box
   :class (style/questions-box)
   :children [[questions-header]
              [shared-views/questions-list]]])

(defn organizer-panel []
  [re-com/h-box
   :class (style/organizer-panel)
   :children [[re-com/box :size "1" :child [questions-box]]
              [re-com/box
               :size "4"
               :child (if (sub ::s/adding-question?)
                        [add-question]
                        [display-question]) ]
              [re-com/v-box
               :class (style/organizer-right-panel)
               :size "1"
               :children [[url]
                          [user-list]]]]])
