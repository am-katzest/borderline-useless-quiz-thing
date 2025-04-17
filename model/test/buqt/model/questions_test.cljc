(ns buqt.model.questions-test
  (:require [buqt.model.questions :as qs]
            [buqt.model.question :as q]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest tally-points-question
  (let [questions {0 (q/question {:type :abcd :count 3})
                   1 (assoc (q/question {:type :abcd :count 3}) :state :revealed)
                   2 (assoc (q/question {:type :abcd :count 3}) :state :revealed :points 0.5)
                   3 (assoc (q/question {:type :abcd :count 3}) :state :active :correct-answer 1)}
        questions-participant (update-vals questions q/censor)]
    (t/testing "max-points"
      (t/is (= 3.5 (qs/max-points questions)))
      (t/is (= 2.5 (qs/max-points questions-participant))))
    (let [ans-wrong {0 1 1 1 2 1 3 0}
          ans-correct {0 0 1 0 2 0 3 1}
          ans-missing {}
          ans-mixed {1 1 2 0}]
      (t/testing "from participant pov")
      (t/testing "from organizer pov"
        (let [check #(qs/tally-points-for-answers-organizer questions %)]
          (t/is (= 0 (check ans-wrong)))
          (t/is (= 3.5 (check ans-correct)))
          (t/is (= 0 (check ans-missing)))
          (t/is (= 0.5 (check ans-mixed)))))
      (t/testing "from organizer pov"
        (let [check #(qs/tally-points-for-answers-participant questions-participant %)]
          (t/is (= {:min 0 :max 1} (check ans-wrong)))
          (t/is (= {:min 1.5 :max 2.5} (check ans-correct)))
          (t/is (= {:min 0 :max 0} (check ans-missing)))
          (t/is (= {:min 0.5 :max 0.5} (check ans-mixed))))))))
