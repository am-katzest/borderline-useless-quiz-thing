(ns buqt.server.id
  (:require [buqt.server.utils :as utils]))

(defonce state (atom {:current-id 0
                  :id->token {}}))

(defn add-token [{:keys [current-id] :as state} token]
  (let [id (inc current-id)]
    (-> state
        (assoc :current-id id)
        (update :id->token assoc id token))))


(defn get-id-and-token! []
  (let [res (swap! state add-token (utils/random-hexstring 40))
        id (:current-id res)
        token (get-in res [:id->token id])]
    {:id id :token token}))

(defn id-token-valid? [id token]
  (and (string? token)
       (int? id)
       (> 10 (count token))
       (= token (get-in @state [:id->token id]))))
