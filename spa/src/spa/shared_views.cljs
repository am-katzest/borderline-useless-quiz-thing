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
         :class (style/questions-list-item (= id (sub ::s/selected-question-id)))}
   [re-com/h-box
    :align :center
    :children
    [(:description question)
     "("
     (:question-type question)
     ")"]]])

(defn questions-list []
  [:div {:class (style/vertically-scrollable)}
   [re-com/v-box
    :gap "10px"
    :class (style/questions-list)
    :children (for [[id question] (sub ::s/questions)]
                [question-list-item id question])]])
