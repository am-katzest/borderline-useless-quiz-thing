(ns buqt.model.impact)


;; when client receives update which isn't one it expect (next one in the queue)
;; it needs to decide whether applying pending optimistic updates is still possible
;; to do this it compares compatibility of each update
;; compatibility consists of
;; :domain -- only updates with the same domain can possibly interact
;; :impact -- defines how update impacts the state; one of
;; * :final -- no other update can happen after this one (like deleting question)
;; * :self-contained -- doesn't interact with any other updates (except final)
;; * numeric -- updates with higher number override those with lower

(defmulti update->compatibility
  (fn [_state update] (:type update)))

(defn possible-after? "can b be applied after a" [a b]
  (cond (= a :final) false ;order doesn't matter, there should't be two final updates
        (= b :final) true
        (= b :self-contained) true
        (= a :self-contained) true
        :else (>= a b)))

(defn update-compatible? [unexpected optimistic]
  (if (not= (:domain unexpected) (:domain optimistic))
    true
    (possible-after? (:impact unexpected) (:impact optimistic))))
