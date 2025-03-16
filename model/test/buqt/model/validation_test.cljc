(ns buqt.model.validation-test
  (:require [buqt.model.validation :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            #?(:clj [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])))

(t/deftest basic-schema-test
  (t/is (s/validate sut/ClientState
                    {:user-type :participant
                     :id 5
                     :cnt 5
                     :username "mrau"}))
  (t/is (s/validate sut/ClientState
                    {:user-type :organizer
                     :id 5
                     :cnt 5
                     :id->name {3 "mrau"}})))
