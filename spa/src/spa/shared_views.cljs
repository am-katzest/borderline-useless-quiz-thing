(ns spa.shared-views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [spa.styles :as style]
   [spa.events :as events]
   [spa.ui-elements :as els]
   [buqt.model.question :as q]
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

(defn reordering-arrow [id count side reorder]
  (let [mode (cond (and (= :right side)  (= 0 id)) :disabled
                   (and (= :left side) (= (dec count) id)) :disabled
                   (= :left side) :down
                   (= :right side) :up)
        id-to-swap-with-lower (if (= mode :up) (dec id) id)]
    (if (= mode :disabled)
      :disabled
      (str mode "(" id-to-swap-with-lower ")"))
    [re-com/md-icon-button
     :md-icon-name (case mode
                     :up "zmdi-long-arrow-up"
                     :down "zmdi-long-arrow-down"
                     :disabled "zdmi-blank")
     :on-click #(reorder id-to-swap-with-lower)
     :disabled? (= mode :disabled)]))

(defn reorder-list [question order change-order]
  [re-com/v-box
     :children
   (let [reorder (fn [x] (change-order (q/swap-with-lower order x)))]
     (doall (for [[absolute relative]
                  (map vector
                       (range (:count question))
                       order)]
              [re-com/h-box
               :padding "5px"
               :align :center
               :children [(reordering-arrow absolute (:count question) :left reorder)
                          [els/fancy-input "" (els/->val-set question [:descriptions relative]) "400px" :disabled? true]
                          (reordering-arrow absolute (:count question) :right reorder)]])))])
