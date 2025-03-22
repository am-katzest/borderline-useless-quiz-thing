(ns spa.utils)

(defn sstr->map [sstr]
  (try (->>
        (-> sstr (.substring 1) (.split "&"))
        (map (fn [x] (let [[a b] (.split x "=")]
                       (when (and a b (not= "" a) (not= "" b))
                         [(keyword a) (keyword b)]))))
        (into {}))
       (catch js/Error _ {})))

(defn map->sstr [m]
  (if-not (pos? (count m)) ""
          (->> m
               (map (fn [[a b]] (str (name a) "=" (name b))))
               (reduce #(str %1 "&" %2))
               (str "?"))))

(defn get-url-info []
  (sstr->map (.. js/window -location -search)))

(defn change-url-query "without reloading" [q]
  (let [hist (.. js/window -history)
        url (new js/URL (.. js/window -location -href))]
    (set!  (.-search url) q)
    (.pushState hist nil nil url)))

(defn store-url-info! [m]
  (change-url-query (map->sstr m)))
