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

(reg-input-event+db
 ::clicked-delete-question
 (fn [db id]
   [(assoc db :current-question nil)
    {:type :input/remove-question
     :question-id id}]))

(re-frame/reg-event-db
 ::show-add-question-ui
 (fn [db _]
   (assoc db :adding-question? true)))
