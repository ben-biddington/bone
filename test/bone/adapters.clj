(ns bone.adapters
  (:import java.lang.String)
  (:refer-clojure :exclude [next])
  (:require [clojure.test :refer :all]
            [bone.timestamps :refer :all :as timestamps]))

(deftest timestamps-are-unix-timestamps
  (testing "that is returns something"
           (let [value (timestamps/next)]
             (is (not (nil? value))))))