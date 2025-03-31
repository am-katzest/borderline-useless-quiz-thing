(ns buqt.model.questions-test
  (:require [buqt.model.questions :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest adding-question-test
  (let [s0 {}
        s1 (sut/add-question s0 {:type :abcd :count 3})
        s2 (sut/add-question s1 {:type :abcd :count 5})]
    (t/is (= 0 (count s0)))
    (t/is (= 1 (count s1)))
    (t/is (= 2 (count s2)))
    (t/is (= [1] (keys s1)))
    (t/is (= 3 (get-in s1 [1 :count])))
    (t/is (= 3 (get-in s2 [1 :count])))
    (t/is (= 5 (get-in s2 [2 :count])))))
