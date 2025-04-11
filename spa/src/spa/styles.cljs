(ns spa.styles
  (:require-macros
    [garden.def :refer [defcssfn]])
  (:require
    [spade.core   :refer [defglobal defclass]]
    [garden.units :refer [deg px]]
    [garden.color :refer [rgba]]))

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

(defclass organizer-url
  []
  {:color :white
   :padding "20px"
   :background-color clr-primary-a20})

(defclass organizer-users-box
  []
  {:color :white
   :padding "20px"
   :background-color clr-primary-a10})

(defclass organizer-users-box-user
  []
  {:color :white
   :margin "5px"
   :padding "5px"
   :width "100%"
   :background-color clr-primary-a20})

(defclass organizer-panel
  []
  {:height "100vh"
   :width "100%"})

(defclass add-question-box
  []
  {:padding "20px"})

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
  {:padding "20px"})

(defclass questions-list-item
  []
  {:background-color clr-primary-a20
   :padding "5px"}
  [:&:hover {:background-color clr-primary-a30}])
