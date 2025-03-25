(ns spa.events
  (:require
   [re-frame.core :as re-frame]
   [spa.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
   [buqt.model.client :as c]
   [cljs.core.async :as a]
   [haslett.format :as fmt]
   [spa.utils :as utils]
   [haslett.client :as ws]))



(def api-root "/api")

(defn request [method path]
  {:method          method
   :uri             (str api-root path)
   :response-format (ajax/json-response-format {:keywords? true})
   :format (ajax/json-request-format)})

(defn add-auth [request auth]
  (-> request 
      (assoc-in [:headers "x-token"] (:token auth))
      (assoc-in [:headers "x-id"] (:id auth))))

(defn add-handlers [request on-success on-failure]
  (assoc request 
         :on-success on-success
         :on-failure on-failure))
(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
