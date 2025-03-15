(ns buqt.model.client-test
  (:require [buqt.model.client :as sut]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest answers_meow
  (t/is (= "meow" (sut/meow))))
