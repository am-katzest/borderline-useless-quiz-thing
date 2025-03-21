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

(t/deftest broker-test
  (t/testing "connecting organizer"
    (let [organizer-id 10
          broker-chan (sut/spawn-broker organizer-id)
          [c a] (make-mock-chan 1)]
      (sut/change-connection! broker-chan organizer-id c)
      (let [res (first @a)]
        (t/is (= :update/reset (:type res)))
        (t/is (= 0 (:cnt res)))
        (t/is (= 0 (:cnt (:state res))))
        (t/is (= 10 (:id (:state res)))))))
  (t/testing "adding user"
    (t/testing "organizer receives a message"
      (let [organizer-id 10
            user-id 5
            broker-chan (sut/spawn-broker organizer-id)
            [c a] (make-mock-chan 2)]
        (sut/change-connection! broker-chan organizer-id c)
        (sut/add-participiant! broker-chan user-id)
        (t/is (= {:type :update/add-participant, :id user-id, :cnt 0} (second @a)))))
    (t/testing "new user gets a reset"
      (let [organizer-id 10
            user-id 5
            broker-chan (sut/spawn-broker organizer-id)
            [c a] (make-mock-chan 1)]
        (sut/add-participiant! broker-chan user-id)
        (sut/change-connection! broker-chan user-id c)
        (t/is (= :update/reset (-> @a first :type)))
        (t/is (= user-id (-> @a first :state :id))))))
  (t/testing "changing username"
    (let [organizer-id 10
          user-id 5
          broker-chan (sut/spawn-broker organizer-id)
          [co ao] (make-mock-chan 3)
          [cu au] (make-mock-chan 2)]
      (sut/add-participiant! broker-chan user-id)
      (sut/change-connection! broker-chan organizer-id co)
      (sut/change-connection! broker-chan user-id cu)
      (sut/send-action! broker-chan {:type :action/change-username :username "new" :id user-id})
      (t/is (= {:type :update/change-username :id user-id :username "new"} (dissoc (last @ao) :cnt)))
      (t/is (= (dissoc (last @ao) :cnt) (dissoc (last @au) :cnt)))))
  (t/testing "invalid message handling"
    (let [organizer-id 10
          user-id 5
          broker-chan (sut/spawn-broker organizer-id)
          [co ao] (make-mock-chan 3)
          [cu au] (make-mock-chan 2)]
      (sut/add-participiant! broker-chan user-id)
      (sut/change-connection! broker-chan organizer-id co)
      (sut/change-connection! broker-chan user-id cu)
      (sut/send-action! broker-chan {:type :action/evil :evilness 2})
      (sut/send-action! broker-chan {:type :action/change-username :username "new" :id user-id})
      (t/is (= {:type :update/change-username :id user-id :username "new"} (dissoc (last @ao) :cnt)))
      (t/is (= (dissoc (last @ao) :cnt) (dissoc (last @au) :cnt))))))
