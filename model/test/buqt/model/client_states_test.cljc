(ns buqt.model.client-states-test
  (:require [buqt.model.client-states :as sut]
            [buqt.model.client :as c]
            [buqt.model.question :as q]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

;; add update confirming empty
;; add update confirming with updates
;; add update diverging empty
;; add update diverging bad
;; add update diverging good bad good

(t/deftest apply-input-test
  (t/testing "onto state with no updates"
    (let [state (sut/->client-states (c/make-participant 0))
          input {:type :input/change-username :username "meow"}
          [state' msgs] (sut/apply-input state input)]
      (t/is (= [(c/input->action (sut/gui-state state) input)] msgs))
      (t/is (= 1 (count (:updates state'))))
      (t/is (= "meow" (:username (sut/gui-state state'))))
      (t/is (not= "meow" (:username (sut/base-state state'))))))
  (t/testing "onto state with predictive updates"
    (let [state (-> (sut/->client-states (c/make-participant 0))
                    (sut/apply-input {:type :input/change-username :username "meo"})
                    first)
          input {:type :input/change-username :username "meow"}
          [state' msgs] (sut/apply-input state input)]
      (t/is (= [(c/input->action (sut/gui-state state) input)] msgs))
      (t/is (= 2 (count (:updates state'))))
      (t/is (= "meow" (:username (sut/gui-state state'))))
      (t/is (not= "meow" (:username (sut/base-state state')))))))


(t/deftest apply-update-test
  (let [question (assoc (q/question {:type :abcd :count 2}) :state :active)
        question-stopped (assoc question :state :stopped)
        question-changed (assoc question :description "meow")
        client
        (sut/->client-states (c/apply-update (c/make-participant 0)
                                             {:type :update/change-question
                                              :id 1
                                              :question question}))]
    (t/testing "updates empty"
      (let [[result msg] (sut/apply-update client {:type :update/change-question
                                                   :id 1
                                                   :cnt 0
                                                   :question question-changed})]
        (t/is (= [] msg))
        (t/is (= 0 (count (:updates result))))
        (t/is (= "meow" (-> result
                            sut/gui-state
                            (get-in [:questions 1 :description]))))))
    (t/testing "creating predictive updates"
      (let [[client [action]] (sut/apply-input client {:type :input/change-answer :question-id 1 :answer 0})
            confirm-change-answer {:type :update/change-answer
                                   :question-id 1
                                   :participant-id 0
                                   :answer 0
                                   :cnt 0}]
        (t/testing "updates structure created correctly"
          (t/is (= 1 (count (:updates client))))
          (let [[[_ _ expected]] (:updates client)]
            (t/is (= expected (:gui client))))
          (t/is (= 1 (:cnt (:gui client))))
          (t/is (= 0 (:cnt (:base client)))))
        (t/is (= :action/change-answer (:type action)))
        (t/testing "confirming"
          (let [[client msgs] (sut/apply-update client confirm-change-answer)]
            (t/is (= [] msgs))
            (t/is (= (:base client) (:gui client)))
            (t/is (= 1 (:cnt (:base client))))
            (t/is (= [] (:updates client)))
            (t/is (= 0 (get-in client [:gui :question->answer 1])))))
        (t/testing "replacing"
          (let [[client msgs] (sut/apply-update client {:type :update/change-question
                                                        :id 1
                                                        :question question-stopped
                                                        :cnt 0})]
            (t/is (= [] msgs))
            (t/is (= [] (:updates client)))
            (t/is (= (:base client) (:gui client)))
            (t/is (= 1 (:cnt (:base client))))
            (t/is (= :stopped (get-in client [:base :questions 1 :state])))))
        (t/testing "reapplying"
          (let [[client msgs] (sut/apply-update client {:type :update/change-question
                                                        :id 1
                                                        :question question-changed
                                                        :cnt 0})]
            (t/is (= [] msgs))
            (t/is (= 1 (count (:updates client))))
            (t/is (not= (:base client) (:gui client)))
            (t/is (= 0 (get-in client [:gui :question->answer 1])))
            (t/is (not= 0 (get-in client [:base :question->answer 1])))
            (t/is (= "meow" (get-in client [:base :questions 1 :description])))
            (t/is (= "meow" (get-in client [:gui :questions 1 :description])))
            (t/is (= 1 (:cnt (:base client))))
            (t/is (= 2 (:cnt (:gui client))))
            (t/testing "confirming after reapplying"
              (let [[client msgs] (sut/apply-update client (c/increment-cnt confirm-change-answer))]
                (t/is (= [] msgs))
                (t/is (= [] (:updates client)))
                (t/is (= (:gui client) (:base client)))
                (t/is (= 0 (get-in client [:base :question->answer 1])))))))))))

(t/deftest reset-test
  (t/testing "asking"
    (t/is (= [{:type :action/ask-for-reset}] (second (sut/apply-update
                                                      (sut/->client-states {:cnt 1 :id 5})
                                                      {:type :meow :cnt 5})))))
  (t/testing "applying"
    (t/is (= [{:base {:meow :mraw}
               :gui {:meow :mraw}
               :updates []} []]
             (sut/apply-update
              (sut/->client-states {:cnt 1 :user-type :participant})
              {:type :update/reset :state {:meow :mraw}})))))
  
