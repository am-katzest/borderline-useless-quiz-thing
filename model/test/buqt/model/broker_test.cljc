(ns buqt.model.broker-test
  (:require
   [buqt.model.broker :as broker]
   [buqt.model.client :as client]
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
