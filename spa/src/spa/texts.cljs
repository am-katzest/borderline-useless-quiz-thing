(ns spa.texts)

(def texts-en
  {:question
   {:type
    {:bools
     {:name "bools"
      :description "multiple single choice questions"}
     :abcd
     {:name "test"
      :description "single answer ABCD style question"}
     :text
     {:name "text"
      :description "text answer, checked manually"}}}})

(defn get-text [m & keys]
  (if-not (map? m) "m isn't a map"
    (let [ans (get-in m keys)]
      (cond
        (string? ans) ans
        (map? ans) (partial get-text ans)
        :else "no such key"))))

(def text (partial get-text texts-en))
