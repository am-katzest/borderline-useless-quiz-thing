(ns spa.view-utils
  (:require [re-frame.core :as re-frame]))

(defn sub [x]
  @(re-frame/subscribe (if (keyword? x) [x] x)))

(defn evt [x]
  (re-frame/dispatch (if (keyword? x) [x] x)))
