(ns spa.organizer.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.organizer.subs :as os]
   [spa.subs :as s]
   [spa.styles :as style]
   [spa.organizer.events :as oe]))

(defn url []
  [re-com/v-box
   :class (style/organizer-url)
   :children
   [[re-com/label :label [:p "here's a link you can copy: "]]
    [:tt {:style {:font-size :small}} (sub ::s/link)]]])

(defn user-list []
  [re-com/v-box
   :class (style/organizer-users-box)
   :size "auto"
   :children
   [[re-com/label :label "users:"]
    (for [u (sub ::os/usernames)]
      [re-com/label
       :class (style/organizer-users-box-user)
       :label (if (and u (not= "" u)) u "[empty]")])]])

(defn organizer-panel []
  [re-com/h-box
   :class (style/organizer-panel)
   :children [[re-com/box :size "1" :child "questions placeholder"]
              [re-com/box :size "4" :child "current question placeholder"]
              [re-com/v-box
               :class (style/organizer-right-panel)
               :size "1"
               :children [[url]
                          [user-list]]]]])
