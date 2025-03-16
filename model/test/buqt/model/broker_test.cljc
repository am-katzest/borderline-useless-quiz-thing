(ns buqt.model.broker-test
  (:require
   [buqt.model.broker :as broker]
   [buqt.model.client :as client]
   #?(:clj [clojure.test :as t]
      :cljs [cljs.test :as t :include-macros true])))

(def broker-a
  {:clients {1 (assoc (client/make-organizer 1) :id->name {2 ""})
             2 (client/make-participant 2)}
   :organizer 1})

(t/deftest process-action-test
  (let [msg-a {:type :action/change-username
               :id 2
               :username "new"}
        [{:keys [clients]} msgs] (broker/process-action broker-a msg-a)]
    (t/is (= "new" (get-in clients [1 :id->name 2])))
    (t/is (= "new" (get-in clients [2 :username])))
    (t/is (= 1 (get-in clients [2 :cnt])))
    (t/is (= 1 (get-in clients [1 :cnt])))
    (t/is (= [[2 {:type :update/change-username, :id 2, :username "new", :cnt 0}]
              [1 {:type :update/change-username, :id 2, :username "new", :cnt 0}]]
             msgs))))
