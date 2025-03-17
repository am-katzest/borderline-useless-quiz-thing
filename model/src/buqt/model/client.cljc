(ns buqt.model.client)


;; doesn't concern itself with `cnt`, that's handled elsewhere™

(defn make-participant [id]
  {:user-type :participant
   :id id
   :cnt 0
   :username ""})

(defn make-organizer [id]
  {:user-type :organizer
   :id id
   :cnt 0
   :id->name {}})

(defmulti  apply-update (fn [state msg] (if (= (:type msg) :update/reset)
                                          :update/reset
                                          [(:user-type state) (:type msg)])))

(defmethod apply-update [:participant :update/change-username]
  [state {:keys [username]}]
  (assoc state :username username))

(defmethod apply-update [:organizer :update/change-username]
  [state {:keys [username id]}]
  (update state :id->name assoc id username))

(defmethod apply-update :update/reset
  [_state {:keys [state]}]
  state)

;; state_x + msg_x = state_{x+1}
(defn taking-care-of-cnt [f]
  (fn [state msg]
    (if (= :update/reset (:type msg))
      (f state msg)
      (if (= (:cnt state) (:cnt msg))
        (update (f state msg) :cnt inc)
        ::cnt-mismatch))))

;; clients holds a vector of states (suboptiamal but idc)
;; [x0 x1 x2 x3 (gui)]
;; when message x0->x1 is received x0 is popped off
;; when message x0->y1 is received vector is replaced by [y1]

(defn gui-state "fancy `last`" [state-vector]
  (assert (vector? state-vector))
  (nth state-vector (dec (count state-vector))))

(defn restv "rest but preserving type" [v]
  (subvec v 1))

(defn- no-msg [x] [x []])

(defn- with-msg [x m] [x [m]])

;; pondering whether or not to unify ingesting update and
(defn apply-update-whole "(states, update) -> (states, msgs)" [state-vector update]
  {:post [(vector? (first %)) (seq (first %))]}
  (let [state (first state-vector)
        state' ((taking-care-of-cnt apply-update) state update)]
    (cond
      (= ::cnt-mismatch state') (with-msg state-vector {:type :action/ask-for-reset :id (:id state)})
      (= state' (second state-vector)) (no-msg (restv state-vector))
      :else (no-msg [state']))))

(defmulti input->action (fn [_s input] (:type input)))
(defmulti action->expected-update (fn [_s action] (:type action)))

(defmethod input->action :input/change-username [state input]
  (assert (= :participant (:user-type state)))
  {:type :action/change-username
   :username (:username input)})

(defmethod action->expected-update :action/change-username [state action]
  {:type :update/change-username
   :username (:username action)})

(defn apply-input-whole "(states, msg) -> (states, msgs)" [state-vector input]
  {:post [(vector? (first %)) (seq (first %))]}
  (let [state (last state-vector)
        action (input->action state input)
        excepted-update (action->expected-update state action)
        state' (assoc (apply-update state excepted-update) :cnt (inc (:cnt state)))]
    (with-msg (conj state-vector state') [action])))

