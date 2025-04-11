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
   [[re-com/label :label "users:"]
    (for [[id u] (sub ::os/users+names)]
      ^{:key id}
      [re-com/label
       :class (style/organizer-users-box-user)
       :label (if (and u (not= "" u)) u "[empty]")])]])

(defn fancy-input [label [val set] width & {:keys [blur?] :or {blur? false}}]
  [re-com/v-box
   :width width
   :children [[re-com/label :label label]
              [re-com/input-text
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
                            [re-com/button :label "delete" :on-click #(evt [::oe/clicked-delete-question id])]]]
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
                [question-state-edit (val-set [:state])]]]))

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
              [re-com/button :label "+" :on-click #(evt ::oe/show-add-question-ui)]]])

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
