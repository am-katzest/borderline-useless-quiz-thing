(ns spa.utils)

(defn sstr->map [sstr]
  (try (->>
        (-> sstr (.substring 1) (.split "&"))
        (map (fn [x] (let [[a b] (.split x "=")]
                       (when (and a b (not= "" a) (not= "" b))
                         [(keyword a) b]))))
        (into {}))
       (catch js/Error _ {})))

(defn map->sstr [m]
  (if-not (pos? (count m)) ""
          (->> m
               (map (fn [[a b]] (str (name a) "=" (str b))))
               (reduce #(str %1 "&" %2))
               (str "?"))))

(defn get-url-info []
  (sstr->map (.. js/window -location -search)))


(defn change-url "without reloading" [url]
  (let [hist (.. js/window -history)]
    (.pushState hist nil nil url)))

(defn url-with-changed-query [q]
  (let [url (new js/URL (.. js/window -location -href))]
    (set!  (.-search url) q)
    url))

(defn store-url-info! [m]
  (change-url (url-with-changed-query (map->sstr m))))

(defn get-url-with-info [m]
  (str (url-with-changed-query (map->sstr m))))

(defn id->str [id]
  (str "token-for-" id))

(defn get-token [id]
  (let [t (.getItem (.-localStorage js/window) (id->str id))]
    (when (and (some? t) (not= "" t)) t)))

(defn store-token! [id token]
  (.setItem (.-localStorage js/window) (id->str id) (str token)))
