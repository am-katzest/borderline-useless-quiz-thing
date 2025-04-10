(ns spa.ui-elements
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [spa.styles :as styles]))

(defn make-validating-setter [model path validate]
  (fn [x]
    (when (validate x)
      (swap! model assoc-in path x))))


(defn +-number-edit [model path validate]
  (let [v (get-in @model path)
        set (make-validating-setter model path validate)]
    [re-com/h-box
     :class (styles/number-edit)
     :children
     [[re-com/button
       :label "-"
       :on-click #(set (dec v))]
      [re-com/input-text
       :model (str v)
       :change-on-blur? false
       :on-change #(set (parse-long %))
       :width "40px"]
      [re-com/button
       :label "+"
       :on-click #(set (inc v))]]]))

