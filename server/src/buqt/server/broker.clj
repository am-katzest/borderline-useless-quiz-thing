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

