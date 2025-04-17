(ns spa.subs
  (:require
   [re-frame.core :as re-frame]
   [spa.utils :as utils]
   [buqt.model.client :as c]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::gui-state
 (fn [db]
   (when-let [state (:state db)]
     (println (c/gui-state state))
     (c/gui-state state))))

(re-frame/reg-sub
 ::user-type
 :<- [::gui-state]
 (fn [state]
   (:user-type state)))

(re-frame/reg-sub
 ::running?
 (fn [db]
   (:running db)))

(re-frame/reg-sub
 ::adding-question?
 (fn [db]
   (:adding-question? db)))

(re-frame/reg-sub
 ::link
 (fn [db]
   (utils/get-url-with-info {:quiz-id (:quiz-id db)})))

(re-frame/reg-sub
 ::questions
 :<- [::id->question]
 (fn [id->question]
   ;; drops hidden and deleted questions
   (filter second id->question)))

(re-frame/reg-sub
 ::id->question
 :<- [::gui-state]
 (fn [state]
   (:questions state)))

(re-frame/reg-sub
 ::selected-question-id
 (fn [db]
   (:current-question db)))

(re-frame/reg-sub
 ::selected-question
 :<- [::gui-state]
 :<- [::selected-question-id]
 (fn [[state qid]]
   (println state qid)
   (get-in state [:questions qid])))
