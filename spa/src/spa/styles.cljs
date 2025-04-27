(ns spa.styles
  (:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.units :refer [deg px]]
    [garden.color :as color]))

(def clr-primary-a0 :#6112a6)
(def clr-primary-a10 :#521488)
(def clr-primary-a20 :#42146c)
(def clr-primary-a30 :#341351)
(def clr-primary-a40 :#251137)
(def clr-primary-a50 :#180b1f)
(def clr-accent-a0 :#12a1a6)
(def clr-accent-a10 :#1b8488)
(def clr-accent-a20 :#1d696b)
(def clr-accent-a30 :#1b4e50)
(def clr-accent-a40 :#173536)
(def clr-accent-a50 :#111e1e)
(def clr-danger-a0 :#b41c2b)
(def clr-danger-a10 :#851d22)
(def clr-danger-a20 :#581919)
(def clr-danger-a30 :#2f1310)
(def clr-success-a0 :#009f42)
(def clr-success-a10 :#167533)
(def clr-success-a20 :#184d25)
(def clr-success-a30 :#132916)
(def clr-warning-a0 :#f0ad4e)
(def clr-warning-a10 :#af7f3c)
(def clr-warning-a20 :#71532a)
(def clr-warning-a30 :#392b19)
(def clr-info-a0 :#388cfa)
(def clr-info-a10 :#3267b5)
(def clr-info-a20 :#284475)
(def clr-info-a30 :#1a253b)
(def clr-neutral-a0 :#ffffff)
(def clr-neutral-a10 :#c6c6c6)
(def clr-neutral-a20 :#919191)
(def clr-neutral-a30 :#5e5e5e)
(def clr-neutral-a40 :#303030)
(def clr-neutral-a50 :#000000)

(defglobal defaults
  [:body
   {:color               clr-neutral-a0
    :background-color    clr-neutral-a40}])

(defclass level1
  []
  {:color :green})

(defclass organizer-right-panel
  []
  {:color :white
   :background-color clr-primary-a30})

(defclass participant-right-panel
  []
  {:color :white
   :padding "20px"
   :background-color clr-primary-a30})

(defclass organizer-url
  []
  {:color :white
   :padding "20px"
   :background-color clr-primary-a20})

(defclass organizer-users-box
  []
  {:color :white
   :padding "20px"
   :padding-left "5px"
   :padding-right "10px"
   :background-color clr-primary-a10})

(defclass organizer-users-box-user
  []
  {:color :white
   :margin "5px"
   :padding "5px"
   :width "100%"
   :background-color clr-primary-a30})

(defclass organizer-panel
  []
  {:height "100vh"
   :width "100%"})

(defclass participant-panel
  []
  {:height "100vh"
   :width "100%"})

(defclass add-question-box
  []
  {:padding "20px"
   :width "100%"
   :background-color clr-primary-a40})

(defclass initial-question-edit-box
  []
  {:padding "20px"})

(defclass number-edit
  []
  {:padding "20px"})

(defclass questions-box
  []
  {:width "100%"
   :background-color clr-primary-a10})

(defclass questions-header
  []
  {:padding "10px"
   :min-height "60px"
   :background-color clr-primary-a20})

(defclass questions-list
  []
  {:background-color clr-primary-a10
   :scrollbar-width :none
   :overflow-y :scroll
   :overflow-x :hidden
   :padding "20px"})

(defclass vertically-scrollable
  []
  {:scrollbar-width :none
   :height "100%"
   :overflow-y :scroll
   :overflow-x :hidden})

(defclass questions-list-item
  [selected]
  {:background-color (if selected clr-primary-a30 clr-primary-a20)
   :padding "5px"}
  [:&:hover {:background-color clr-primary-a30}])

(defclass question-edit
  []
  {:padding "20px"
   :overflow-y :scroll
   :overflow-x :hidden
   :scrollbar-width :none
   :background-color clr-primary-a40
   :width "100%"})

(defclass question-participant
  []
  {:padding "20px"
   :background-color clr-primary-a40
   :width "100%"})

(defclass abcd-question-btn
  [correct?]
  {:background-color (if correct? clr-success-a20 clr-danger-a20)}
  [:&:hover {:background-color clr-success-a10
             :color :white}])

(defclass abcd-question-answer-btn
  [selected? correct? known? editable?]
  {:background-color (cond editable? (if selected? clr-neutral-a10 clr-neutral-a30)
                           known? (case [selected? correct?]
                                    [true true] clr-success-a10
                                    [true false] clr-danger-a10
                                    [false true] clr-success-a30
                                    [false false] clr-danger-a30)
                           :else (if selected? clr-neutral-a20 clr-neutral-a40))}
  [:&:hover (when editable?
              {:background-color clr-neutral-a10
               :color :white})])

(defclass fancy-input
  []
  {:background-color "#333"
   :border-width "1px"
   :border-color :black
   :color "white"}
  [:&:disabled {:background-color "#333" :color "#aaa"}])

(defclass question-description
  []
  {:padding "10px"
   :width "100%"
   :background-color clr-primary-a20})

(defclass points-box
  []
  {:padding "10px"
   :padding-right "5px"
   :background-color clr-primary-a20})

(defclass text-grade-btn
  [selected?]
  {:font-size :x-small
   :margin "5px"
   :padding "3px"
   :min-width "35px"
   :color :white
   :background-color (if selected? clr-primary-a10 clr-primary-a40)}
  [:&:hover {:background-color clr-primary-a0
              :color :white}])

(defclass bool-btn
  [val]
  {:background-color (if val :white :black)
   :color (if val :black :white)}
  [:&:hover {:background-color :#888
             :color (if val :white :black)}])

(defclass bool-display
  [val]
  {:width "1.5em"
   :height "1.5em"
   :border-radius "0.3em"
   :background-color (if val :white :black)
   :color (if val :black :white)})

(defclass bool-edit-btn
  [val known? editable? correct?]
  {:background-color (if known?
                       (if correct? clr-success-a0 clr-danger-a0)
                       (if val :white :black))
   :opacity 1
   :color (if val :black :white)}
  [:&:hover (if editable? {:background-color :#888
                           :color (if val :white :black)}
                {:color (if val :black :white)})])

(defclass pillbox []
  {:color :white}
  [:li
   {:background-color clr-primary-a20
    :border-radius "5px"}
   [:a {:color :white}
    [:&:hover {:background-color  clr-primary-a0 :border-radius "5px"}]]]
  [:li.active {:background-color clr-primary-a0}])

(defclass text-button [color]
  {:background-color color
   :border :none
   :color :white}
  [:&:hover {:background-color (color/lighten (color/as-rgb (name color)) 10)
             :color :white}]
  [:&:focus {:border :none :color :white}])
