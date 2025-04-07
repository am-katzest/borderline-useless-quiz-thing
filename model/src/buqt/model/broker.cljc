(ns buqt.model.broker
  (:require [buqt.model.client :as client]
            [buqt.model.question :as q]
            [buqt.model.questions :as qs]
            [buqt.model.utils :as u]))
;; broker holds state of each client
;; and likely some other internal state as well
;; but we'll see about that
;; there are two layers, external handling authentication (not shown here)
;; and internal, handling state & dispatching messages
;; there's only one interface

;; state contains all the newest client states
;; {:clients {13 {} 12 {}} :organizer 12}

(defn organizer-id [broker] (:organizer broker))
(defn organizer [broker] (get (:clients broker) (:organizer broker)))
(defn participant-ids [broker] (remove #{(:organizer broker)} (keys (:clients broker))))
(defn client [broker id] (get-in broker [:clients id]))
(defn participants [broker] (map #(client broker %) (participant-ids broker)))
(defn questions [broker] (:questions (organizer broker)))
(defn question [broker question-id] ((questions broker) question-id))
(defn question-as [broker id question-id] ((:questions (client broker id)) question-id))

(defmulti dispatch-msgs (fn [_b action] (:type action)))

(defmethod dispatch-msgs :action/change-username
  [broker {:keys [id username] :as action}]
  (u/participant** broker action)
  (let [msg {:type :update/change-username :id id :username username}]
    [broker
     [[id msg]
      [(organizer-id broker) msg]]]))

(defn send-question-updates [broker id question]
  (let [msg 
        {:type :update/change-question
         :id id
         :question question}
        censored-msg (update msg :question q/censor)]
    [broker
     (conj
      (for [pid (participant-ids broker)]
        [pid censored-msg])
      [(organizer-id broker) msg])]))

(defmethod dispatch-msgs :action/add-question
  [broker {:keys [desc] :as action}]
  (u/organizer** broker action)
  (let [id (qs/next-question-id (questions broker))
        question (q/question desc)]
    (send-question-updates broker id question)))

(defmethod dispatch-msgs :action/update-question
  [broker {question' :question id :question-id :as action}]
  (u/organizer** broker action)
  (let [question ((questions broker) id)]
    (u/assert* question' "no question with this id")
    (u/assert* (q/update-valid? question question') "update invalid"))
  (send-question-updates broker id question'))

(defmethod dispatch-msgs :action/add-participant
  [broker {:keys [id]}]
  [(update broker :clients assoc id (client/make-participant id))
   [[(organizer-id broker) {:type :update/add-participant :id id}]]])

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

(defn init-broker [organizer-id]
  {:organizer organizer-id
   :clients {organizer-id (client/make-organizer organizer-id)}})
