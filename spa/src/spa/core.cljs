(ns spa.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [spa.utils :as u]
   [spa.events :as events]
   [spa.views :as views]
   [spa.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (let [stored (or (u/get-url-info) {})
        quiz-id (:quiz-id stored)
        id (some->  stored :user-id parse-long)
        token (some-> id u/get-token)]
    (cond
      (and quiz-id id token)
      (re-frame/dispatch-sync [::events/start-connection id quiz-id token])
      quiz-id
      (re-frame/dispatch-sync [::events/join-quiz quiz-id])
      :else
      (re-frame/dispatch-sync [::events/create-quiz])))
  (dev-setup)
  (mount-root))
