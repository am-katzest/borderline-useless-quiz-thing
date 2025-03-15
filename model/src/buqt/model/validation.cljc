(ns buqt.model.validation
  (:require #?(:clj [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])
            #?(:clj [schema.experimental.abstract-map :as am]
               :cljs [schema.experimental.abstract-map :as am :include-macros true])))

;; todo: possibly add a predicate that checks if id is one of the correct ones?

(s/defschema ClientState
  (am/abstract-map-schema
   :user-type
   {:id s/Int
    :cnt s/Int}))

(am/extend-schema Organizer ClientState [:organizer]
                  {:participants {s/Int s/Str}})

(am/extend-schema Participant ClientState [:participant]
                  {:name s/Str})

