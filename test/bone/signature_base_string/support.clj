(ns bone.signature-base-string.support
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.support :refer :all]
            [bone.signature-base-string :refer :all]
            [ring.util.codec :refer :all]))

(defn param[name,value] (struct parameter name value))
(def debug? (= "ON" (System/getenv "LOUD")))
