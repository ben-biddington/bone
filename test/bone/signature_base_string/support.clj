(ns bone.signature-base-string.support
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.signature-base-string :refer :all]
            [ring.util.codec :refer :all]))

(defn param[name,value] (struct parameter name value))
(def debug? (= "ON" (System/getenv "LOUD")))
(defn must-contain[text expected] (is (.contains text expected) (str "Expected <" text "> to include <" expected ">")))
(defn must-not-contain[text expected] (is (not (.contains text expected)) (str "Expected <" text "> to exclude <" expected ">")))
(defn must-equal[text expected] (is (= text expected) (str "Expected <" text "> to equal <" expected ">")))
