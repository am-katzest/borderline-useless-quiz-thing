(ns buqt.model.utils)

(defn assert*
  ([p] (when-not p (throw (ex-info "assert failed" {}))))
  ([p t] (when-not p (throw (ex-info t {}))))
  ([p t m] (when-not p (throw (ex-info t m)))))    

(defn participant* [state]
  (assert* (= :participant (:user-type state))))

(defn organizer* [state]
  (assert* (= :organizer (:user-type state))))

