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

(defn fancy-input [label [val set] width & {:keys [blur?] :or {blur? false}}]
  [re-com/v-box
   :width width
   :children [[re-com/label :label label]
              [re-com/input-text
               :class (styles/fancy-input)
               :style {:width width}
               :model val
               :change-on-blur? blur?
               :on-change set]]])

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
