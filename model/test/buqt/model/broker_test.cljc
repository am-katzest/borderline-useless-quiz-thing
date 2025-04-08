(ns buqt.model.broker-test
  (:require
   [buqt.model.broker :as broker]
   [buqt.model.client :as client]
   [buqt.model.question :as q]
   #?(:clj [clojure.test :as t]
      :cljs [cljs.test :as t :include-macros true])))

(def organizer-1 (assoc (client/make-organizer 1) :id->name {2 ""}))
(def participant-2 (client/make-participant 2))
(def broker-a
  {:clients {1 organizer-1
             2 participant-2}
   :organizer 1})

(t/deftest process-action-test
  (let [msg-a {:type :action/change-username
               :id 2
               :username "new"}
        [{:keys [clients]} msgs] (broker/process-action broker-a msg-a)]
    (t/is (= "new" (get-in clients [1 :id->name 2])))
    (t/is (= "new" (get-in clients [2 :username])))
    (t/is (= 1 (get-in clients [2 :cnt])))
    (t/is (= 1 (get-in clients [1 :cnt])))
    (t/is (= [[2 {:type :update/change-username, :id 2, :username "new", :cnt 0}]
              [1 {:type :update/change-username, :id 2, :username "new", :cnt 0}]]
             msgs))))

(t/deftest username-only-for-participant-test
  (t/is (thrown? clojure.lang.ExceptionInfo (broker/process-action broker-a {:type :action/change-username :id 1 :username "new"}))))

(t/deftest reset-test
  (let [msg-a {:type :action/ask-for-reset :id 1}
        [broker msgs] (broker/process-action broker-a msg-a)]
    (t/is (= broker broker-a))
    (t/is (= [[1 {:type :update/reset :cnt 0 :state organizer-1}]] msgs))))

(t/deftest user-management-test
  (t/is (= {:organizer 10
            :clients {10 (client/make-organizer 10)}}
           (broker/init-broker 10)))
  (t/is
   (= [[10 {:type :update/add-participant, :id 20, :cnt 0}]]
      (second (broker/process-action (broker/init-broker 10)
                                     {:type :action/add-participant :id 20}))))
  (t/is (= {20 ""}
           (get-in (broker/process-action
                    (broker/init-broker 10)
                    {:type :action/add-participant :id 20})
                   [0 :clients 10 :id->name]))))

(defn message-to [msgs id]
  (->> msgs
       (filter (comp #{id} first))
       first
       second))

(t/deftest send-question-updates-test
  (let [q (q/question {:type :abcd :count 3})
        [broker msgs] (broker/send-question-updates broker-a 5 q)]
    (t/is (= broker broker-a))
    (t/is (= 2 (count msgs)))
    (let [to-organizer (message-to msgs (:id organizer-1))
          to-participant (message-to msgs (:id participant-2))]
      (doseq [msg msgs]
        (t/is (= :update/change-question (:type (second msg)))))
      (t/is (= true (q/validate (:question to-organizer))))
      (t/is (= 0 (:correct-answer (:question to-organizer))))
      (t/is (not= 0 (:correct-answer (:question to-participant))))
      (t/is (= (:id to-organizer) (:id to-participant))))
    (t/testing "no participants"
      (let [broker (update broker-a :clients dissoc 2)
            [broker' msgs] (broker/send-question-updates broker 5 q)]
        (t/is (= broker' broker))
        (t/is (= 1 (count msgs)))
        (t/is (= 0 (:correct-answer (:question (message-to msgs (:id organizer-1)))))))) msgs))

(t/deftest adding-question-test
  (let [[broker' msgs] (broker/process-action
                        broker-a
                        {:type :action/add-question
                         :desc {:type :abcd :count 3}
                         :id 1})
        organizer (broker/organizer broker')
        participants (broker/participants broker')]
    (t/is (= 1 (first (keys (:questions organizer)))))
    (t/is (= :abcd (->> organizer :questions vals first :question-type)))
    (t/is (= 0 (->> organizer :questions vals first :correct-answer)))
    (t/is (= nil (->> participants first :questions second)))
    (t/is (not= 0 (->> participants first :questions vals first :correct-answer)))))
