(ns bone.about-signature-base-strings
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.core :refer :all]))

; <http://oauth.net/core/1.0a/#anchor13>
;; The Signature Base String is a consistent reproducible concatenation of the request elements into a single string. 
;; The string is used as an input in hashing or signing algorithms. 
;; The HMAC-SHA1 signature method provides both a standard and an example of using the Signature Base String with a signing algorithm to generate signatures. 
;; All the request parameters MUST be encoded as described in Parameter Encoding;; prior to constructing the Signature Base String.

(defn- signature-base-string[opts]
  (str ""))

(deftest normalizing-request-parameters
  (let [result (signature-base-string {:auth-header {:realm "http://sp.example.com/"}})]
    (testing "that it omits realm"
      (is (= false (.contains result "realm="))))
    
    ))
