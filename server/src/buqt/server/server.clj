(ns buqt.server.server
  (:require [org.httpkit.server :as hk-server]
            [reitit.coercion.spec]
            [reitit.ring :as ring]
            [ring.middleware.json :as json]
            [buqt.server.id :as id]
            [taoensso.timbre :as log]
            [clojure.edn :as edn]
            [buqt.server.broker :as broker]
            [reitit.ring.middleware.parameters :as parameters]))

(defn id-auth-middleware [handler]
  (fn [{headers :headers
        :as request}]
    (let [token (headers "x-token")
          id (some-> (headers "x-id") parse-long)]
      (if (id/id-token-valid? id token)
        (handler (assoc request :user-id id))
        {:status 403 :body "invalid id or token"}))))

(def http-app
  (ring/ring-handler
   (ring/router
    ["/api"
     [ ;; endpoints without auth
      ["/id" {:get {:handler (fn [_] {:status 200 :body (id/get-id-and-token!)})}}]
      ;; with auth
      ["/quiz" {:middleware [id-auth-middleware]}
       ["" {:post {:handler
                   (fn [{:keys [user-id]}]
                     {:status 200
                      :body {:id (broker/create-quiz! user-id)}})}}]]]]
    {:data {:coercion   reitit.coercion.spec/coercion
            :middleware [parameters/parameters-middleware
                         json/wrap-json-response]}})))

(defn ws-app [request]
  (hk-server/as-channel
   request
   (let [state (atom :new)]
     {:on-receive (fn [ch msg]
                    (try (let [decoded (edn/read-string msg)]
                           (log/debugf "received ws message %s" decoded)
                           (if (= @state :new)
                             (let [{:keys [type id quiz-id token]} decoded]
                               (if-let [broker-chan
                                        (and (= type :handshake)
                                             (id/id-token-valid? id token)
                                             (broker/get-broker quiz-id))]
                                 (let [send! #(hk-server/send! ch (pr-str %))]
                                   (broker/add-participiant! broker-chan id)
                                   (broker/change-connection! broker-chan id send!)
                                   (reset! state [id broker-chan])
                                   (send! :ok))
                                 (hk-server/close ch)))
                             (broker/send-action! (second @state) (assoc decoded :id (first @state)))))
                         (catch Throwable _ (hk-server/close ch))))})))

(defn app [request]
  ((if (:websocket? request) ws-app http-app)
   request))

(defn run-server [conf]
  (hk-server/run-server #'app conf))

(comment (def stop-server (run-server {:port 8091})))
