(ns buqt.model.combined-tests
  (:require [buqt.model.broker :as broker]
            [buqt.model.client :as client]
            [buqt.model.question :as q]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(defn make-broker "returns broker, 0 -- organizer, 1.. -- participants"
  [n-participants]
  (reduce
   (fn [broker id]
     (first
      (broker/process-action
       broker
       {:type :action/add-participant :id id})))
   (broker/init-broker 0)
   (map inc (range n-participants))))

(defn process-action [broker action]
  (first (broker/process-action broker action)))

(defn process-input [broker id input]
  (let [client (broker/client broker id)
        action (client/input->action client input)
        [broker' _] (broker/process-action broker (assoc action :id id))]
    (let [predicted-update (client/action->expected-update client action)
          client-on-server (broker/client broker' id) ;; client after "server" update
          client-on-client (client/apply-update client predicted-update)] ;; client after "predicted" update
      (t/testing "predicted update identical"
        (t/is (= (dissoc client-on-server :cnt)
                 (dissoc client-on-client :cnt)))))
    broker'))

