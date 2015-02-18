(ns bone.signature-base-string.about-request-verb
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.signature-base-string.support :refer :all]
            [ring.util.codec :refer :all]))

(deftest request-verb 
  (let [result (signature-base-string { :verb "get" })]
      (testing "that it upper-cases verb"
        (must-not-contain result "get")
        (must-contain result "GET"))))
