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
 ::link
 (fn [db]
   (utils/get-url-with-info {:quiz-id (:quiz-id db)})))
