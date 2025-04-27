(ns spa.db)

(def default-db
  {:name "re-frame"
   :current-question nil
   :adding-question? true
   :state {:gui nil
           :base nil
           :updates nil}})
