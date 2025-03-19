(ns buqt.server.utils
  (:import (java.security SecureRandom)))

;; https://stackoverflow.com/questions/7111651/how-to-generate-a-secure-random-alphanumeric-string-in-java-efficiently#7111735
(defn random-hexstring [length]
  (let [rng (SecureRandom.)
        chars "abcdef0123456789"
        random-char #(get chars (.nextInt rng (count chars)))]
    (apply str (repeatedly length random-char))))
