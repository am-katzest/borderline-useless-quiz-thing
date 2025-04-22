(ns buqt.model.client-test
  (:require [buqt.model.client :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(def participant-a
  {:user-type :participant
   :id 5
   :username "mrau"})

(def organizer-a
  {:user-type :organizer
   :id 3
   :id->name {5 "mrau"}})

(t/deftest username-tests
  (t/testing "applying update raw"
    (let [msg {:id 5 :username "mrrrp" :type :update/change-username}]
      (t/testing "to participant"
        (t/is (= "mrrrp" (:username (sut/apply-update participant-a msg)))))
      (t/testing "to organizer"
        (t/is (= {5 "mrrrp"} (:id->name (sut/apply-update organizer-a msg))))))))

(t/deftest adding-participant-test
  (let [added (sut/apply-update (sut/make-organizer 0) {:type :update/add-participant :id 5})]
    (t/is (= {5 ""} (:id->name added)))
    (t/is (= {5 {}} (:participant->question->answer added)))))

(t/deftest reset-test
  "todo")

(t/deftest question-creation-test
  (let [organizer (sut/make-organizer 1)
        input {:type :input/add-question :desc {:type :abcd :count 3}}
        action (sut/input->action organizer input)
        update (sut/action->expected-update organizer action)
        organizer' (sut/apply-update organizer update)]
    (t/is (= 1 (count (:questions organizer'))))
    (t/is (= :abcd (:question-type (first (vals (:questions organizer'))))))))

(t/deftest reset-works-on-invalid-clients
  (t/is (= participant-a (sut/apply-update nil {:type :update/reset :state participant-a})))
  (t/is (= participant-a (sut/apply-update {:user-type :meow} {:type :update/reset :state participant-a}))))

