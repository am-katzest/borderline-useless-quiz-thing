(ns spa.organizer.events
  (:require
   [re-frame.core :as re-frame]
   [spa.events :as e :refer [reg-input-event reg-input-event+db]]))

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

(reg-input-event
 ::question-updated
 (fn [id body]
   {:type :input/update-question
    :question-id id
    :question body}))

(re-frame/reg-event-db
 ::show-add-question-ui
 (fn [db _]
   (assoc db :adding-question? true)))
