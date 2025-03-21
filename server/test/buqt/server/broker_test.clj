(ns buqt.server.broker-test
  (:require [buqt.server.broker :as sut]
            [clojure.core.async :as a]
            [clojure.test :as t]))

(defn read-first-n-from-chan [chan n]
  (loop [n n acc []]
    (if (zero? n) acc
      (let [v (a/alt!!
                [(a/timeout 100)] ::timeout
                [chan] ([v] v))]
        (if (= v ::timeout) acc
            (recur (dec n) (conj acc v))))
        )))

(defn make-mock-chan [n]
  (let [chan (a/chan (+ 100 n))]
    [#(a/>!! chan %) (delay (read-first-n-from-chan chan n))]))

(t/deftest mocked-chan-test
  (let [[f res] (make-mock-chan 3)]
    (f 1)
    (f 2)
    (t/is (= [1 2] @res)))
  (let [[f res] (make-mock-chan 3)]
    (f 1)
    (f 2)
    (f 3)
    (t/is (= [1 2 3] @res)))
  (let [[f res] (make-mock-chan 3)]
    (f 1)
    (f 2)
    (f 3)
    (f 4)
    (t/is (= [1 2 3] @res))))

(t/deftest sender-test
  (let [c (sut/make-sender)
        [c1 a1] (make-mock-chan 1)
        [c2 a2] (make-mock-chan 2)]
    (a/>!! c :3)
    (a/>!! c c1)
    (a/>!! c :4)
    (t/is (= [:4] @a1))
    (a/>!! c c2)
    (a/>!! c :5)
    (a/>!! c :6)
    (t/is (= [:5 :6] @a2))))

(t/deftest process-msg-test
  (let [broker (sut/create-broker 1)]
    (t/testing "adding-user"
      (let [[broker-with-user msgs] (sut/process-msg broker [:add-participant 5])]
        (t/is (= msgs [[1 {:type :update/add-participant, :id 5, :cnt 0}]]))
        (t/is (= 2 (count (:clients (:broker broker-with-user)))))))
    (t/testing "replacing-connection"
      (let [[broker-with-replaced-connection msgs] (sut/process-msg broker [:change-connection [1 :3]])]
        (t/is (= 2 (count msgs)))
        (t/is (= [1 :3] (first msgs)))
        (t/is (= :update/reset (:type (second (second msgs)))))
        (t/is (= broker broker-with-replaced-connection))))
    (t/testing "processing actions"
      (let [[broker-with-replaced-connection msgs] (sut/process-msg broker [:action {:type :action/ask-for-reset :id 1}])]
        (t/is (= 1 (count msgs)))
        (t/is (= :update/reset (:type (second (first msgs)))))
        (t/is (= broker broker-with-replaced-connection))))))
