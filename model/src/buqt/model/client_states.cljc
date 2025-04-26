(ns buqt.model.client-states
  (:require [buqt.model.client :as c]
            [buqt.model.impact :as i]))

;; client-states is a map with the following keys:
;; :base -- state according to last server update
;; :gui -- state with all optimistic updates applied
;; :updates -- vec of [update compatibility result] between base-state and gui-state

(defn ->client-states [state]
  {:base state
   :gui state
   :updates []})

(defn gui-state "selects a state for gui to display" [client-states]
  (:gui client-states))

(defn base-state "selects a state aligned with last server update" [client-states]
  (:base client-states))

;; TODO: what if this function gets called with expected update?
;; TODO: compatibility of updates consumed along the way shouldn't be ingested, right?
;; why did i do this

(defn reconcile-divergent-updates
  "client-states, update -> client-states"
  [{:keys [base updates]} received-update]
  (let [base' (c/apply-update base received-update)
        compatibility-to-test-against (i/update->compatibility base received-update)]
    (loop [current base'
           [[update compatibility] & updates-todo] updates
           updates-done []]
      (if (nil? update)
        {:base base'
         :gui current
         :updates updates-done}
        (if (i/update-compatible? compatibility-to-test-against compatibility)
          (let [current (c/apply-update current update)]
            (recur
             current
             updates-todo
             (conj updates-done [update compatibility current])))
          (recur current updates-todo updates-done))))))

(defn- restv "rest but preserving type" [v]
  (subvec v 1))

(defn ingest-update "returns nil when update differs from expected" [client-states update]
  (let [state (:base client-states)
        [[_ _ expected]] (:updates client-states)
        actual ((c/taking-care-of-cnt c/apply-update) state update)]
    (cond
      (nil? expected)
      (-> client-states
          (assoc :base actual)
          (assoc :gui actual))
      (= actual expected)
      (-> client-states
          (clojure.core/update :updates restv)
          (assoc :base actual)))))

(defn- no-msg [x] [x []])

(defn- with-msg [x m] [x [m]])

;; check cnt at the beginning?
(defn apply-update "(client-states, update) -> (client-states, msgs)" [client-states update]
  (cond
    (= :update/reset (:type update)) [(->client-states (:state update)) []]
    (not (c/cnt-matches? (:base client-states) update)) (with-msg client-states {:type :action/ask-for-reset})
    :else (no-msg (or (ingest-update client-states update)
                      (reconcile-divergent-updates client-states update)))))




(defn add-optimistic-update [client-states action]
  (let [state (:gui client-states)
        predicted-update (c/action->expected-update state action)
        compatibility (i/update->compatibility state predicted-update)
        state' (c/increment-cnt (c/apply-update state predicted-update))]
    (-> client-states
        (assoc :gui state')
        (update :updates conj [predicted-update compatibility state']))))

(defn apply-input "(client-states, msg) -> (client-states, msgs)" [client-states input]
  (let [state (:gui client-states)
        action (c/input->action state input)]
    (with-msg (add-optimistic-update client-states action) action)))
