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
  (try (condp = type
         :action (let [[model' msgs] (m/process-action (:broker broker) body)]
                   [(assoc broker :broker model') msgs])
         :add-participant (if (get-in broker [:output-chans body])
                            [broker []]
                            (process-msg
                             (update broker :output-chans assoc body (make-sender))
                             [:action {:type :action/add-participant :id body}]))
         :change-connection (let [[id conn] body]
                              (insert-msgs-before (process-msg broker [:action {:type :action/ask-for-reset :id id}])
                                                  [[id conn]])))
       (catch Throwable _ [broker []])))

(defn run-broker [broker]
  (a/go-loop [broker broker]
    (if-let [msg (a/<! (:input-chan broker))]
      (let [[broker' msgs] (process-msg broker msg)]
        (doseq [[addr content] msgs
                :let [chan (get-in broker' [:output-chans addr])]]
          (log/debugf "queueing %s to be sent to %s (%s)" content addr chan)
          (when chan
            (a/>! chan content)))
        (recur broker'))
      (doseq [[_id chan] (:output-chans broker)]
        (a/close! chan)))))

(defn spawn-broker [organizer-id]
  (let [broker (create-broker organizer-id)]
    (run-broker broker)
    (:input-chan broker)))

(defn add-participiant! [broker-chan id]
  (a/>!! broker-chan [:add-participant id]))

(defn change-connection! [broker-chan id f]
  (a/>!! broker-chan [:change-connection [id f]]))

(defn send-action! [broker-chan a]
  (a/>!! broker-chan [:action a]))

(defonce quiz-id->broker-chan (atom {}))

(defn create-quiz! [organizer-id]
  (let [broker-chan (spawn-broker organizer-id)
        quiz-id (utils/random-hexstring 10)]
    (swap! quiz-id->broker-chan assoc quiz-id broker-chan)
    quiz-id))
