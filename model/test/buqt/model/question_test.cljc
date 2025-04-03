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
