(ns spa.organizer.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.organizer.subs :as os]
   [reagent.core :as r]
   [spa.subs :as s]
   [spa.styles :as style]
   [spa.shared-views :as shared-views]
   [buqt.model.question :as q]
   [spa.ui-elements :as els]
   [spa.events :as e]
   [spa.organizer.events :as oe]))

(defn url []
  [re-com/v-box
   :class (style/organizer-url)
   :children
   [[re-com/label :label [:p "here's a link you can copy: "]]
    [:tt {:style {:font-size :small}} (sub ::s/link)]]])

(defn user-list []
  [:div {:class (style/vertically-scrollable)}
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
                 :label (if (and u (not= "" u)) u "[empty]")]]]))]]])

(defn question-state-edit [[value set]]
  [re-com/v-box
   :children [[re-com/label :label "set question state"]
              [re-com/horizontal-pill-tabs
               :class (style/pillbox)
               :model value
               :on-change set
               :tabs [{:id :hidden :label "hidden"}
                      {:id :visible :label "visible"}
                      {:id :active :label "active"}
                      {:id :stopped :label "stopped"}
                      {:id :revealed :label "revealed"}]]]])

(defmulti edit-question-type-specific (fn [question val-set]
                                        (:question-type question)))

(defmethod edit-question-type-specific
  :abcd
  [question val-set]
  [re-com/v-box
   :gap "10px"
   :children
   [[re-com/label :label "possible answers:"]
    (doall (for [[letter i] (map vector q/letters (range (:count question)))]
             ^{:key i}
             [re-com/h-box
              :align :center
              :padding "5px"
              :gap "20px"
              :children [[re-com/button
                          :class (style/abcd-question-btn (= i (:correct-answer question)))
                          :label letter
                          :on-click #((second (val-set [:correct-answer])) i)]
                         [els/fancy-input "" (val-set [:possible-answers i]) "400px" ]]]))
    [re-com/label :label "participant answers:"]
    [re-com/v-box
     :children (doall (for [[id username] (sub ::os/users+names)
                            :let [answer (sub [::os/participant-answer-for-selected-question id])]]
                        ^{:key id}
                        [re-com/h-box
                         :class (style/organizer-users-box-user)
                         :gap "5px"
                         :children [[re-com/label :style {:min-width "100px" :text-align :right} :label username]  ":" (get q/letters answer "")]]
                        ))]]])

(defmethod edit-question-type-specific
  :bools
  [question val-set]
  [re-com/v-box
   :gap "10px"
   :children
   [[re-com/label :label "true/false statements:"]
    (doall (for [i (range (:count question))]
             ^{:key i}
             [re-com/h-box
              :align :center
              :padding "5px"
              :gap "20px"
              :children [[els/bool-toggle (val-set [:key i])]
                         [els/fancy-input "" (val-set [:descriptions i]) "400px" ]]]))
    [re-com/label :label "participant answers:"]
    [re-com/v-box
     :children (doall (for [[id username] (sub ::os/users+names)
                            :let [answer (sub [::os/participant-answer-for-selected-question id])]]
                        ^{:key id}
                        [re-com/h-box
                         :class (style/organizer-users-box-user)
                         :gap "5px"
                         :children [[re-com/label :style {:min-width "100px" :text-align :right} :label username]  ":"
                                    (when answer
                                      [re-com/h-box
                                       :align :center
                                       :gap "6px"
                                       :children
                                       (for [bool answer]
                                         [:div {:class (style/bool-display bool)}])])]]
                        ))]]])

(defn text-answer-rater [points [val set]]
  [re-com/h-box
   :align :center
   :children [[re-com/label :label "points:"]
              (for [[key x] (map-indexed vector (concat (range 0 points 0.5) [points]))
                    :let [selected? (or (and (= x 0) (nil? val)) (= x val))]]
                ^{:key key}
                [re-com/button :class (style/text-grade-btn selected?) :label (str x) :on-click #(set x)])]])

(defmethod edit-question-type-specific
  :text
  [question val-set]
  [re-com/v-box :children
   [[re-com/label :label "participant answers:"]
    [re-com/v-box
     :children (doall (for [[id username] (sub ::os/users+names)
                            :let [answer (sub [::os/participant-answer-for-selected-question id])]]
                        ^{:key id}
                        [re-com/v-box
                         :class (style/organizer-users-box-user)
                         :children [
                                    [re-com/h-box
                                     :gap "5px"
                                     :children [[re-com/label :style {:min-width "100px" :text-align :right} :label username]  ":" answer]]
                                    (when answer [text-answer-rater
                                                  (:points question)
                                                  (val-set [:answer->points answer])])]]))]]])

(defn question-edit []
  (let [question (sub ::s/selected-question)
        id (sub ::s/selected-question-id)
        val-set (els/make-val-set question #(evt [::oe/question-updated id %]))]
    [re-com/v-box
     :gap "20px"
     :class (style/question-edit)
     :children [[re-com/h-box
                 :justify :between
                 :children [[re-com/label :label "edit question!"]
                            [re-com/button
                             :class (style/text-button style/clr-danger-a10)
                             :label "delete" :on-click #(evt [::oe/clicked-delete-question id])]]]
                [re-com/h-box
                 :width "100%"
                 :gap "20px"
                 :align :end
                 :children
                 [[els/fancy-input "description" (val-set [:description]) "400px"]
                  [els/fancy-input "points" (val-set [:points]
                                                 :display str
                                                 :coerce parse-double
                                                 :validate #(not (neg? %))
                                                 ) "50px" :blur? true]]]
                [question-state-edit (val-set [:state])]
                [edit-question-type-specific question val-set]]]))

(defn display-question []
  (if (sub ::s/selected-question)
    [question-edit]
    (do (evt ::e/goto-question-with-highest-number) "no question selected")))

(defmulti initial-question-edit (fn [type _] type))
(defmulti initial-question-type-state identity)

(defmethod initial-question-type-state :abcd []
  {:count 4})

(defmethod initial-question-type-state :bools []
  {:count 4})

(defmethod initial-question-type-state :text []
  {})

(defmethod initial-question-edit :abcd [_type desc]
  [re-com/v-box
   :children
   [[re-com/label :label "number of answers:"]
    [els/+-number-edit desc [:count] #(<= 2 % 20)]]])

(defmethod initial-question-edit :bools [_type desc]
  [re-com/v-box
   :children
   [[re-com/label :label "number of true/false statements:"]
    [els/+-number-edit desc [:count] #(<= 1 % 20)]]])

(defmethod initial-question-edit :text [_type desc])

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
                      :class (style/pillbox)
                      :model question-type
                      :on-change (fn [type]
                                   (reset! question-type type)
                                   (reset! question-state (initial-question-type-state type)))
                      :tabs [{:id :abcd :label "abcd"}
                             {:id :text :label "text"}
                             {:id :bools :label "bools"}]]
                     [re-com/button
                      :class (style/text-button style/clr-primary-a20)
                      :label "add question!"
                      :on-click #(evt [::oe/add-question @question-type @question-state])]]]
                   [re-com/box :class (style/initial-question-edit-box) :child [initial-question-edit @question-type question-state]]]])]))


(defn questions-header []
  [re-com/h-box
   :class (style/questions-header)
   :justify :between
   :align :center
   :children [[re-com/label :style {:width "20px"} :label "questions:"]
              [re-com/button :class (style/text-button style/clr-primary-a40)
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
