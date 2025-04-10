(ns spa.organizer.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.organizer.subs :as os]
   [spa.subs :as s]
   [spa.organizer.events :as oe]))

(defn organizer-top-bar []
  [re-com/h-box
   :children
   [[re-com/label :label [:p "here's a link you can copy: "
                          [:tt (sub ::s/link)]]]
    ]])

(defn organizer-user-list []
  [re-com/v-box
   :style {:background-color "#ccc"
           :color "#000"}
   :children
   [[re-com/label :label "users:"]
    (for [u (sub ::os/usernames)]
      [re-com/label :label (str "-" u)])
    ]])

(defn organizer-panel []
  [re-com/v-box
   :children [[organizer-top-bar]
              [organizer-user-list]]])
