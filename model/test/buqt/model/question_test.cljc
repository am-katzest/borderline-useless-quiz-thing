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
            :possible-answers ["" "" "" "" ""]
            :correct-answer 0}
           (sut/question {:type :abcd :count 5})))
  (t/is (thrown? clojure.lang.ExceptionInfo (sut/question {:type :abcd :count 0})))
  (t/is (thrown? clojure.lang.ExceptionInfo (sut/question {:type :abcd :count 50}))))

(t/deftest checking-test
  (t/is (= 1 (sut/grade (sut/question {:type :abcd :count 5}) 0)))
  (t/is (= 0 (sut/grade (sut/question {:type :abcd :count 5}) 3)))
  (t/is (= 0 (sut/grade (sut/question {:type :abcd :count 5}) nil)))
  (t/is (= 0 (sut/grade nil nil))))

(t/deftest validation-test
  (let [q (sut/question {:type :abcd :count 2})]
    (t/is (not= true (sut/validate (update q :count inc))))
    (t/is (= true (sut/validate q)))
    (t/testing "schema validation"
      (t/is (not= true (sut/validate (assoc q :meow :3))))
      (t/is (not= true (sut/validate (assoc q :possible-answers ["meow" 2 3 4]))))
      (t/is (not= true (sut/validate (assoc q :description :3))))
      (t/is (not= true (sut/validate (dissoc q :description))))
      (t/is (not= true (sut/validate (assoc q :state :meow))))
      (t/is (not= true (sut/validate (assoc q :points -2)))))))

(t/deftest update-validation-test
  (let [q2 (sut/question {:type :abcd :count 2})
        q3 (sut/question {:type :abcd :count 3})]
    (t/is (= true (sut/update-valid? q2 q2)))
    (t/is (= false (sut/update-valid? q2 q3)))))

(t/deftest censoring-test
  (let [base-q (sut/question {:type :abcd :count 3})
        q #(assoc base-q :state %)
        partially-visible (dissoc base-q :correct-answer)]
    (doseq [[state correct]
            [[:hidden nil]
             [:visible partially-visible]
             [:active partially-visible]
             [:stopped partially-visible]
             [:revealed base-q]]]
      (t/testing state
        (t/is (= (some-> correct (assoc :state state))
                 (sut/censor (q state))))))))

(t/deftest answer-validity-test
  (let [q (sut/question {:type :abcd :count 4})]
    (t/is (= true (sut/validate-answer q 3)))
    (t/is (= true (sut/validate-answer q 0)))
    (t/is (= false (sut/validate-answer q nil)))
    (t/is (= false (sut/validate-answer q -1)))
    (t/is (= false (sut/validate-answer q 4)))))

(t/deftest answer-chargeability-test
  (let [q (sut/question {:type :abcd :count 4})
        q-changeable (assoc q :state :active)]
    (t/is (= true (sut/can-change-answer? q-changeable 3)))
    (t/is (= false (sut/can-change-answer? q 3)))
    (t/is (= true (sut/can-change-answer? q-changeable nil)))
    (t/is (= false (sut/can-change-answer? q nil)))))
(t/deftest text-type-test
  (let [q1 (sut/question {:type :text})
        q2 (assoc q1 :answer->points {"meow" 1
                                        "mraw" 0.5}
                  :points 2)]
    (t/testing "validation"
      (t/testing "positive" (t/is (= true (sut/validate q1)))
                 (t/is (= true (sut/validate q2))))
      (t/testing "negative"
        (t/is (= false (sut/validate (assoc q1 :answer->fraction {"meow" 5}))))
        (t/is (= false (sut/validate (assoc q1 :answer->fraction {"meow" -10}))))
        (t/is (= false (sut/validate (assoc q2 :points 0.5))))))
    (t/testing "grading"
      (t/is (== 0 (sut/grade q1 "meow")))
      (t/is (== 1 (sut/grade q2 "meow")))
      (t/is (== 0.5 (sut/grade q2 "mraw"))))))

(t/deftest bools-type-test
   (let [q1 (sut/question {:type :bools :count 3})
        q2 (assoc q1 :key [true false true]
                  :points 2)]
    (t/testing "validation"
      (t/testing "positive" (t/is (= true (sut/validate q1)))
                 (t/is (= true (sut/validate q2))))
      (t/testing "negative"
        (t/is (= false (sut/validate (assoc q1 :descriptions ["meow" "mraw"]))))
        (t/is (= false (sut/validate (assoc q1 :key '(true false true)))))
        (t/is (= false (sut/validate (assoc q1 :description '("" "" "")))))))
    (t/testing "grading"
      (t/is (== 0 (sut/grade q1 [false false false])))
      (t/is (== 1 (sut/grade q1 [true true true])))
      (t/is (== 2/3 (sut/grade q2 [true true false])))
      (t/is (== 0 (sut/grade q2 [0 nil "meow"]))))))

(t/deftest order-type-test
  (let [q1 (sut/question {:type :order :count 3})
        q2 (assoc q1 :correct-order [0 2 1] :points 2)]
    (t/testing "validation"
      (t/testing "positive"
        (t/is (= true (sut/validate q1)))
        (t/is (= true (sut/validate q2))))
      (t/testing "negative"
        (t/is (= false (sut/validate (assoc q1 :descriptions ["meow" "mraw"]))))
        (t/is (= false (sut/validate (assoc q1 :key '(0 1 2)))))
        (t/is (= false (sut/validate (assoc q1 :key [0 1 5]))))
        (t/is (= false (sut/validate (assoc q1 :key [0 1 1]))))
        (t/is (= false (sut/validate (assoc q1 :key [0 1 1 2]))))
        (t/is (= false (sut/validate (assoc q1 :description '("" "" "")))))))))
