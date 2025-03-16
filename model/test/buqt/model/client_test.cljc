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

(t/deftest username-change-success
  (let [msg {:id 5 :username "mrrrp" :type :change-username}]
    (t/testing "to participant"
      (t/is (= "mrrrp" (:username (sut/apply-update participant-a msg)))))
    (t/testing "to organizer"
      (t/is (= {5 "mrrrp"} (:id->name (sut/apply-update organizer-a msg)))))))
