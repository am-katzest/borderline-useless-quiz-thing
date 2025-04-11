(ns spa.organizer.events
  (:require
   [re-frame.core :as re-frame]
   [spa.events :as e :refer [reg-input-event]]))

(re-frame/reg-event-fx
 ::add-question
 (fn [{db :db} [_ type desc]]
   {:db (assoc db :adding-question? false)
    :dispatch [::e/process-input {:type :input/add-question
                                  :desc (assoc desc :type type)}]}))

(re-frame/reg-event-db
 ::show-add-question-ui
 (fn [db _]
   (assoc db :adding-question? true)))
