(ns buqt.model.questions-test
  (:require [buqt.model.questions :as qs]
            [buqt.model.question :as q]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest tally-points-question
  (let [questions {0 (q/question {:type :abcd :count 3})
                   1 (assoc (q/question {:type :abcd :count 3}) :state :revealed)
                   2 (assoc (q/question {:type :abcd :count 3}) :state :revealed :points 0.5)
                   3 (assoc (q/question {:type :abcd :count 3}) :state :active)}
        questions-participant (update-vals questions q/censor)]
    (t/testing "max-points"
      (t/is (= 3.5 (qs/max-points questions)))
      (t/is (= 2.5 (qs/max-points questions-participant))))))
