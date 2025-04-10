(ns spa.organizer.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.organizer.subs :as os]
   [reagent.core :as r]
   [spa.subs :as s]
   [spa.styles :as style]
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

(defn display-question []
  "display question placeholder")

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

(defn organizer-panel []
  [re-com/h-box
   :class (style/organizer-panel)
   :children [[re-com/box :size "1" :child "questions placeholder"]
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
