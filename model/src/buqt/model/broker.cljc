(ns buqt.model.broker
  (:require [buqt.model.client :as client]))
;; broker holds state of each client
;; and likely some other internal state as well
;; but we'll see about that
;; there are two layers, external handling authentication (not shown here)
;; and internal, handling state & dispatching messages
;; there's only one interface

;; state contains all the newest client states
;; {:clients {13 {} 12 {}} :organizer 12}

(defn- organizer-id [broker] (:organizer broker))
(defn- organizer [broker] (get (:clients broker) (:organizer broker)))
(defn- client [broker id] (get-in broker [:clients id]))

(defmulti dispatch-msgs (fn [_b action] (:type action)))

(defmethod dispatch-msgs :action/change-username
  [broker {:keys [id username]}]
  (let [msg {:type :update/change-username :id id :username username}]
    [broker
     [[id msg]
      [(organizer-id broker) msg]]]))

(defmethod dispatch-msgs :action/ask-for-reset
  [broker {:keys [id]}]
  (let [msg {:type :update/reset :state (client broker id)}]
    [broker
     [[id msg]]]))

(defn add-msgs-cnts [broker msgs]
  (let [cnt (fn [id]
              (:cnt (client broker id)))
        add-cnt (fn [[target body]]
                  [target (assoc body :cnt (cnt target))])]
    (mapv add-cnt msgs)))

(defn update-local-clients "applies updates to local client states" [broker msgs]
  (update broker :clients
          (fn [clients]
            (reduce (fn apply-msg [clients [id body]]
                      (update clients id (client/taking-care-of-cnt client/apply-update) body))
                    clients msgs))))

(defn process-action "(broker, action) -> (broker, msgs)" [broker action]
  (let [[broker' msgs] (dispatch-msgs broker action)
        msg+cnts (add-msgs-cnts broker' msgs)
        broker'' (update-local-clients broker' msg+cnts)]
    [broker'' msg+cnts]))
