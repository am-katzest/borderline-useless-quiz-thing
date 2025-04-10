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
        (t/is (= {5 "mrrrp"} (:id->name (sut/apply-update organizer-a msg)))))))
  (t/testing "applying update whole"
    (let [msg {:cnt 3 :username "mrrrp" :type :update/change-username}
          initial-state [(assoc participant-a :cnt 1)]]
      (t/testing "success-replacing"
        (t/is (= [[(assoc participant-a :cnt 2 :username "mrrrp")] []]
                 (sut/apply-update-whole initial-state (assoc msg :cnt 1)))))
      (t/testing "success-skipping"
        (t/is (= [[(assoc participant-a :cnt 2 :username "mrrrp")
                   (assoc participant-a :cnt 3 :username ":3")] []]
                 (sut/apply-update-whole
                  [(assoc participant-a :cnt 1)
                   (assoc participant-a :cnt 2 :username "mrrrp")
                   (assoc participant-a :cnt 3 :username ":3")]
                  (assoc msg :cnt 1)))))
      (t/testing "failure-skipping"
        (t/is (= [[(assoc participant-a :cnt 2 :username "mrrrp")] []]
                 (sut/apply-update-whole
                  [(assoc participant-a :cnt 1)
                   (assoc participant-a :cnt 2 :username "other")
                   (assoc participant-a :cnt 3 :username ":3")]
                  (assoc msg :cnt 1)))))))

  (t/testing "applying input"
    (let [input {:username "mrrrp" :type :input/change-username}
          initial-state [(assoc participant-a :cnt 1)]]
      (t/testing "input lands in"
        (t/is (=
               [[(assoc participant-a :cnt 1 :username "mrau")
                 (assoc participant-a :cnt 2 :username "mrrrp")]
                [{:type :action/change-username, :username "mrrrp"}]]
               (sut/apply-input-whole initial-state input)))))))

(t/deftest adding-participant-test
  (let [added (sut/apply-update (sut/make-organizer 0) {:type :update/add-participant :id 5})]
    (t/is (= {5 ""} (:id->name added)))
    (t/is (= {5 {}} (:participant->question->answer added)))))

(t/deftest reset-test
  (t/testing "asking"
    (t/is (= [{:type :action/ask-for-reset :id 5}] (second (sut/apply-update-whole [{:cnt 1 :id 5}] {:type :meow :cnt 5})))))
  (t/testing "applying"
    (t/is (= [{:meow :mraw}] (first (sut/apply-update-whole [{:cnt 1 :user-type :participant}] {:type :update/reset :state {:meow :mraw}}))))))

(t/deftest question-creation-test
  (let [organizer (sut/make-organizer 1)
        input {:type :input/add-question :desc {:type :abcd :count 3}}
        action (sut/input->action organizer input)
        update (sut/action->expected-update organizer action)
        organizer' (sut/apply-update organizer update)]
    (t/is (= 1 (count (:questions organizer'))))
    (t/is (= :abcd (:question-type (first (vals (:questions organizer'))))))))

