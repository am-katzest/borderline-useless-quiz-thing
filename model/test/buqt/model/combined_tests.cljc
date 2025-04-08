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


(t/deftest updating-questions-test
  (let [broker (process-input (make-broker 1)
                              0
                              {:type :input/add-question
                               :desc {:type :abcd :count 3}})
        [id question] (first (broker/questions broker))]
    (t/is (= :abcd (:question-type question)))
    (t/testing "simple change"
      (let [question' (assoc question :description "meow")
            broker' (process-input broker 0 {:type :input/update-question
                                             :question-id id
                                             :question question'})]
        (t/is (= question' (broker/question broker' id)))
        (t/is (= nil (broker/question-as broker' 1 id)))))
    (t/testing "negative"
      (let [question-fails #(t/is (thrown? clojure.lang.ExceptionInfo (process-input broker %1 %2)))]
        (t/testing "validation"
          (question-fails
           0
           {:type :input/update-question
            :question-id id
            :question (assoc question :state :meow)}))
        (t/testing "context-dependent validation"
          (question-fails
           0
           {:type :input/update-question
            :question-id id
            :question (assoc question :count 2 :possible-answers ["meow" "mraw"])}))
        (t/testing "participant"
          (question-fails
           1
           {:type :input/update-question
            :question-id id
            :question (assoc question :description "meow")}))
        (t/testing "invalid question id"
          (question-fails
           0
           {:type :input/update-question
            :question-id 5
            :question (assoc question :description "meow")}))))
    (t/testing "state change"
      (let [question' (assoc question :state :visible)
            broker' (process-input broker 0 {:type :input/update-question
                                             :question-id id
                                             :question question'})]
        (t/is (= question' (broker/question broker' id)))
        (t/is (= (q/censor question') (broker/question-as broker' 1 id))))
      (let [question' (assoc question :state :revealed)
            broker' (process-input broker 0 {:type :input/update-question
                                             :question-id id
                                             :question question'})]
        (t/is (= question' (broker/question broker' id)))
        (t/is (= (q/censor question') (broker/question-as broker' 1 id)))))))
