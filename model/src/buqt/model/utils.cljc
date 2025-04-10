(ns buqt.model.utils)

(defn assert*
  ([p] (when-not p (throw (ex-info "assert failed" {}))))
  ([p t] (when-not p (throw (ex-info t {}))))
  ([p t m] (when-not p (throw (ex-info t m)))))    

(defn- make-user-assert [expected get]
  (fn [& args]
    (let [actual (apply get args)]
      (assert* (= expected actual) "wrong user type" {:expected expected :actual actual}))))

(def participant* (make-user-assert :participant :user-type))

(def organizer* (make-user-assert :organizer :user-type))

(defn- user-type-for-broker [broker action]
  (->> action :id ((:clients broker)) :user-type))

(def participant** (make-user-assert :participant user-type-for-broker))

(def organizer** (make-user-assert :organizer user-type-for-broker))
