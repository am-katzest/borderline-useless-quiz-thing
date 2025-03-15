(ns buqt.model.validation
  (:require #?(:clj [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])
            #?(:clj [schema.experimental.abstract-map :as am]
               :cljs [schema.experimental.abstract-map :as am :include-macros true])))

;; todo: possibly add a predicate that checks if id is one of the correct ones?

(s/defschema Organizer
  {:user-type (s/eq :organizer)
   :id s/Int
   :cnt s/Int
   :participants {s/Int s/Str}})

(s/defschema Participant
  {:user-type (s/eq :participant)
   :id s/Int
   :cnt s/Int
   :name s/Str})

(s/defschema ClientState
  (s/if #(= :participant (:user-type %)) Participant Organizer))
