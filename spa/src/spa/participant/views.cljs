(ns spa.participant.views
  (:require
   [re-com.core :as re-com]
   [spa.view-utils :refer [sub evt]]
   [spa.participant.subs :as ps]
   [spa.shared-views :as shared-views]
   [spa.ui-elements :as els]
   [spa.styles :as styles]
   [spa.participant.events :as pe]))

(defn username-edit-panel []
  (let [val-set ((els/make-val-set {:username (sub ::ps/username)}
                                   #(evt [::pe/changed-username (:username %)]))
                 [:username])]
    [els/fancy-input "username" val-set "200px"]))

(defn participant-panel []
  [re-com/h-box
   :class (styles/participant-panel)
   :children [[re-com/v-box
               :size "1"
               :class (styles/questions-box)
               :children [[shared-views/questions-list]]]
              [re-com/box
               :size "5"
               :child "meow" ]]])
