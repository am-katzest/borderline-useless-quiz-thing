(ns spa.ui-elements
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [reagent.core :as r]
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

(defn fancy-input [label [val set] width & {:keys [blur? disabled?] :or {blur? false disabled? false}}]
  [re-com/v-box
   :width width
   :children [[re-com/label :label label]
              [re-com/input-text
               :class (styles/fancy-input)
               :style {:width width}
               :model val
               :disabled? disabled?
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

(defn bool-toggle [[val set]]
  [re-com/button
   :class (styles/bool-btn val)
   :label (if val "T" "F")
   :on-click #(set (not val))])

(defn hover-popover [anchor popover & [position time]]
  (let [state (r/atom {:id 0
                       :showing? false})
        change (fn [state showing?] (-> state
                                       (assoc :showing? showing?)
                                       (update :id inc)))
        show (fn [state] (change state true))
        hide (fn [state] (change state false))

        swap-if-id-is-unchanged
        (fn [state id-at-the-time-of-hover]
          (if (= id-at-the-time-of-hover (:id state))
            (show state)
            state))

        make-swapper-that-checks-id
        (fn []
          (let [id-at-the-time-of-hover (:id @state)]
            #(swap! state swap-if-id-is-unchanged id-at-the-time-of-hover)))]
    [(fn []
       [:div {:on-mouse-out #(swap! state hide)
              :on-mouse-over #(js/setTimeout (make-swapper-that-checks-id) (or time 500))}
        [re-com/popover-anchor-wrapper
         :showing? (:showing? @state)
         :anchor anchor
         :popover [re-com/popover-content-wrapper  :style {:color :black} :body  popover]
         :position (or position :below-center)]])]))
