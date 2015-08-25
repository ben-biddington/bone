(ns bone.timestamps
  (:refer-clojure :exclude [next]))

(defn next[] (quot (System/currentTimeMillis) 1000))