(ns buqt.model.client)


;; doesn't concern itself with `cnt`, that's handled elsewhere™
(defmulti  apply-update (fn [state msg] [(:user-type state) (:type msg)]))

(defmethod apply-update [:participant :change-username]
  [state {:keys [username]}]
  (assoc state :username username))

(defmethod apply-update [:organizer :change-username]
  [state {:keys [username id]}]
  (update state :id->name assoc id username))
