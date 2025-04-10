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
