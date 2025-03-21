(ns buqt.server.broker
  (:require [buqt.model.broker :as m]
            [clojure.core.async :as a]
            [taoensso.timbre :as log]))

;; uselessly overengineered, but it takes some complexity away from run-broker
(defn make-sender "returns a channel, when input is a function, swaps f, calls f on subsequent inputs" []
  (let [chan (a/chan (a/sliding-buffer 100))]
    (a/go-loop [f nil]
      (when-let [x (a/<! chan)]
        (log/debugf "sender received %s" x)
        (recur
         (cond
           (fn? x) x
           :else
           (try (f x) f (catch Throwable _ f))))))
    chan))


(defn create-broker [organizer-id]
  (let [model (m/init-broker organizer-id)]
    {:broker model
     :input-chan (a/chan 100)
     :output-chans {organizer-id (make-sender)}}))

(defn insert-msgs-before [[broker msgs] more-msgs]
  [broker (concat more-msgs msgs)])

(defn process-msg [broker [type body]]
  (condp = type
    :action (let [[model' msgs] (m/process-action (:broker broker) body)]
              [(assoc broker :broker model') msgs])
    :add-participant (process-msg
                      (update broker :output-chans assoc body (make-sender))
                      [:action {:type :action/add-participant :id body}])
    :change-connection (let [[id conn] body]
                         (insert-msgs-before (process-msg broker [:action {:type :action/ask-for-reset :id id}])
                                             [[id conn]]))))
