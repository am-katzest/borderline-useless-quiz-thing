(ns buqt.server.broker-test
  (:require [buqt.server.broker :as sut]
            [clojure.core.async :as a]
            [clojure.test :as t]))

(t/deftest sender-test
  (let [c (sut/make-sender)
        p1 (promise)
        p2 (a/chan 10)]
    (a/>!! c :3)
    (a/>!! c #(deliver p1 %))
    (a/>!! c :4)
    (t/is (= :4 (deref p1 100 :missing)))
    (a/>!! c #(a/>!! p2 %))
    (a/>!! c :5)
    (a/>!! c :6)
    (t/is (= :5 (a/<!! p2)))
    (t/is (= :6 (a/<!! p2)))))
