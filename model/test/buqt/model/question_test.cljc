(ns buqt.model.question-test
  (:require [buqt.model.question :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest initialization-test
  (t/is (= {:description "..."
            :question-type :abcd
            :points 1
            :state :hidden
            :count 5
            :possible-answers ["A" "B" "C" "D" "E"]
            :correct-answer 0}
           (sut/question {:type :abcd :count 5})))
  (t/is (thrown? clojure.lang.ExceptionInfo (sut/question {:type :abcd :count 0})))
  (t/is (thrown? clojure.lang.ExceptionInfo (sut/question {:type :abcd :count 50}))))

(t/deftest checking-test
  (t/is (= 1 (sut/grade (sut/question {:type :abcd :count 5}) 0)))
  (t/is (= 0 (sut/grade (sut/question {:type :abcd :count 5}) 3))))

