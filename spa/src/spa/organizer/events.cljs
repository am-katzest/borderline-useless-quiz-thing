(ns spa.organizer.events
  (:require
   [re-frame.core :as re-frame]
   [spa.events :as e :refer [reg-input-event+db]]))

(reg-input-event+db
 ::add-question
 (fn [db type desc]
   [(assoc db :adding-question? false)
    {:type :input/add-question
     :desc (assoc desc :type type)}]))

(re-frame/reg-event-db
 ::show-add-question-ui
 (fn [db _]
   (assoc db :adding-question? true)))
