(ns spa.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [spa.styles :as styles]
   [spa.view-utils :refer [sub evt]]
   [spa.organizer.views :refer [organizer-panel]]
   [spa.participant.views :refer [participant-panel]]
   [spa.subs :as subs]))

(defn page-of-text [text]
  [re-com/title
   :label text
   :level :level1
   :class (styles/level1)])



(defn user-panel []
  (condp = (sub ::subs/user-type)
    :organizer [organizer-panel]
    :participant [participant-panel]
    "error :("))

(defn main-panel []
  (if (sub ::subs/running?)
    [user-panel]
    [page-of-text "connecting"]
    ))
