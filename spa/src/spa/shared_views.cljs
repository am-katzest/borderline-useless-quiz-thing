(ns spa.shared-views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [spa.styles :as style]
   [spa.events :as events]
   [spa.subs :as s]
   [spa.view-utils :refer [evt sub]]))

(defn question-list-item [id question]
  ^{:key id}
  [:div {:on-click #(evt [::events/clicked-question-on-list id])
         :class (style/questions-list-item)}
   [re-com/h-box
    :align :center
    :children
    [(:description question)
     "("
     (:question-type question)
     ")"]]])

(defn questions-list []
  [re-com/v-box
   :class (style/questions-list)
   :gap "10px"
   :children (for [[id question] (sub ::s/questions)]
               [question-list-item id question])])
