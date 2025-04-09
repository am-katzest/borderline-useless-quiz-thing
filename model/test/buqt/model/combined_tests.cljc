(ns buqt.model.combined-tests
  (:require [buqt.model.broker :as broker]
            [buqt.model.client :as client]
            [buqt.model.question :as q]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))
(defn process-action [broker action]
  (first (broker/process-action broker action)))

(defn make-broker "returns broker, 0 -- organizer, 1.. -- participants"
  [n-participants]
  (reduce
   (fn [broker id]
     (process-action
      broker
      {:type :action/add-participant :id id}))
   (broker/init-broker 0)
   (map inc (range n-participants))))

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

(defn update-question [broker id f]
  (let [question (broker/question broker id)]
    (process-input broker 0 {:type :input/update-question
                             :question-id id
                             :question (f question)})))

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

(t/deftest changing-answers-test
  (let [broker (-> (make-broker 2)
                   (process-input 0 {:type :input/add-question
                                     :desc {:type :abcd :count 3}})
                   (process-input 0 {:type :input/add-question
                                     :desc {:type :abcd :count 4}}))
        [q1 q2] (keys (broker/questions broker))
        broker (update-question broker q1 #(assoc % :state :active))]
    (t/testing "positive"
      (let [broker' (process-input
                     broker
                     1
                     {:type :input/change-answer
                      :question-id q1
                      :answer 2})]
        (t/is (= {2 {} 1 {1 2}} (:participant->question->answer (broker/organizer broker'))))
        (t/is (= {} (:question->answer (broker/client broker' 2))))
        (t/is (= {1 2} (:question->answer (broker/client broker' 1))))))
    (let [valid-action {:type :action/change-answer
                        :id 1
                        :question-id q1
                        :answer 0}
          check-if-action-fails #(t/is (thrown? clojure.lang.ExceptionInfo (process-action broker %)))]
      (t/testing "correct action doesn't fail"
        (t/is (some? (process-action broker valid-action))))
      (t/testing "client sending malformed action"
        (t/testing "despite answers being disabled"
          (check-if-action-fails (assoc valid-action :question-id q2)))
        (t/testing "for wrong question"
          (check-if-action-fails (assoc valid-action :question-id 13)))
        (t/testing "with invalid body"
          (check-if-action-fails (assoc valid-action :answer "meow")))))))

(t/deftest newly-joined-clients-receive-questions
  (let [broker (-> (make-broker 1)
                   (process-input 0 {:type :input/add-question
                                     :desc {:type :abcd :count 3}})
                   (update-question 1 #(assoc % :state :visible))
                   (process-action {:type :action/add-participant :id 2}))]
    (t/is (= (broker/question-as broker 1 1)
             (broker/question-as broker 2 1)))))
