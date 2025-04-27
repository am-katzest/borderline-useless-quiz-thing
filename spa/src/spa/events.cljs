(ns spa.events
  (:require
   [re-frame.core :as re-frame]
   [spa.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
   [buqt.model.client-states :as cs]
   [cljs.core.async :as a]
   [haslett.format :as fmt]
   [spa.utils :as utils]
   [haslett.client :as ws]))


(re-frame/reg-fx
 :send-msgs
 (fn [[db msgs]]
   (when-let [send (:send db)]
    (doseq [msg msgs]
      (println "sending" msg)
      (send msg)))))

(def api-root "/api")

(defn start-connection [user-id quiz-id token]
  (a/go
    (js/console.log "starting connection")
    (let [stream (a/<! (ws/connect ;; "ws://localhost:8642/api/connect"
                        "ws://localhost:8091"
                        {:format fmt/edn}))]
      (a/>! (:out stream) {:type :handshake  :id user-id :quiz-id quiz-id :token token})
      (js/console.log "sent handshake")
      (if (= :ok (a/<! (:in stream)))
        (do
          (js/console.log "handshake accepted")
          (re-frame/dispatch [::connected  user-id quiz-id
                              (fn [msg]
                                (a/go (a/>! (:out stream) msg)))])
          (loop []
            (if-let [update (a/<! (:in stream))]
              (do
                (println "we have an update!" update)
                (re-frame/dispatch [::apply-update update])
                (recur))
              (re-frame/dispatch [::disconnected])))
          )
        (do
          (js/console.log "stream closed")
          (re-frame/dispatch [::error])
          (ws/close stream))))))

(defn request [method path]
  {:method          method
   :uri             (str api-root path)
   :response-format (ajax/json-response-format {:keywords? true})
   :format (ajax/json-request-format)})

(defn add-auth [request auth]
  (-> request 
      (assoc-in [:headers "x-token"] (:token auth))
      (assoc-in [:headers "x-id"] (:id auth))))

(defn add-handlers [request on-success on-failure]
  (assoc request 
         :on-success on-success
         :on-failure on-failure))


(defn get-id-request [on-success]
  (-> (request :get "/id")
      (add-handlers
       [::store-token on-success]
       [::error])))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::apply-update
 (fn [{:keys [db]} [_ update]]
   (let [state (:state db)
         [state' msgs] (cs/apply-update state update)]
     {:db (assoc db :state state')
      :send-msgs [db msgs]})))

(re-frame/reg-event-fx
 ::process-input
 (fn [{:keys [db]} [_ input]]
   (let [state (:state db)
         [state' msgs] (cs/apply-input state input)]
     {:db (assoc db :state state')
      :send-msgs [db msgs]})))

(re-frame/reg-event-fx
 ::start-connection
 (fn [_ [_ user-id quiz-id token]]
   (start-connection user-id quiz-id token)
   {}))

(re-frame/reg-event-fx
 ::join-quiz
 (fn [_ [_ quiz-id]]
   {:http-xhrio (get-id-request [::join-continue quiz-id])}))

(re-frame/reg-event-fx
 ::join-continue
 (fn [_ [_ quiz-id {user-id :id token :token}]]
   {:dispatch [::start-connection user-id quiz-id token]}))

(re-frame/reg-event-fx
 ::store-token
 (fn [_ [_ continuation {user-id :id token :token :as res}]]
   (utils/store-token! user-id token)
   {:dispatch (conj continuation res)}))

(re-frame/reg-event-fx
 ::create-quiz
 (fn [_ _]
   {:http-xhrio (get-id-request [::create-quiz-got-id])}
   ))

(re-frame/reg-event-fx
 ::create-quiz-got-id
 (fn [_ [_ auth]]
   {:http-xhrio (->
                 (request :post "/quiz")
                 (add-auth auth)
                 (add-handlers
                  [::quiz-created-success auth]
                  [::error])
                 (assoc :body {}))}
   ))

(re-frame/reg-event-fx
 ::quiz-created-success
 (fn [_ [_ {user-id :id token :token} {quiz-id :id}]]
   {:dispatch [::start-connection user-id quiz-id token]}
   ))

(re-frame/reg-event-fx
 ::error
 (fn [_ evt]
   (js/alert (pr-str "error" evt))))

(re-frame/reg-event-fx
 ::connected
 (fn [{db :db} [_ user-id quiz-id send!]]
   (utils/store-url-info! {:user-id user-id :quiz-id quiz-id})
   {:db (assoc db :send send! :running true :quiz-id quiz-id)}
   ))

(defn reg-input-event [key f]
  (re-frame/reg-event-fx
   key
   (fn [_ [_ & args]]
     {:dispatch [::process-input (apply f args)]})))

(defn reg-input-event+db [key f]
  (re-frame/reg-event-fx
   key
   (fn [{db :db} [_ & args]]
     (let [[db input] (apply f db args)]
       {:db db
        :dispatch [::process-input input]}))))

(re-frame/reg-event-db
 ::clicked-question-on-list
 (fn [db [_ id]]
   (assoc db :current-question id :adding-question? false)))

(re-frame/reg-event-db
 ::goto-question-with-highest-number
 (fn [db _]
   (if-let [newest-question
            (->> db
                 :state
                 cs/gui-state
                 :questions
                 (filter second)
                 (map first)
                 (apply max))]
     (assoc db :current-question newest-question :adding-question? false)
     db)))
