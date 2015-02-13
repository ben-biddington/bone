(ns bone.about-signature-base-strings
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.core :refer :all]
            [ring.util.codec :refer :all]))

; <http://oauth.net/core/1.0a/#anchor13>
;; The Signature Base String is a consistent reproducible concatenation of the request elements into a single string. 
;; The string is used as an input in hashing or signing algorithms. 
;; The HMAC-SHA1 signature method provides both a standard and an example of using the Signature Base String with a signing algorithm to generate signatures. 
;; All the request parameters MUST be encoded as described in Parameter Encoding;; prior to constructing the Signature Base String.

(defn- earl-encode[what] (ring.util.codec/url-encode what))

(defn- %[what] (earl-encode what))

(defn- name [what] (-> what %))
(defn- value[what] (-> (if (nil? what) "" what) %))

(defn- signature-base-string[parameters]
  (str 
   (name "oauth_consumer_key=")      (value (-> parameters :auth-header :oauth-consumer-key))
   (name "oauth_token=")             (value (-> parameters :auth-header :oauth-token))
   (name "oauth_signature_method=")  (value (-> parameters :auth-header :oauth-signature-method))
   (name "oauth_timestamp=")         (value (-> parameters :auth-header :oauth-timestamp))
   (name "oauth_nonce=")             (value (-> parameters :auth-header :oauth-nonce))
   (name "oauth_version=")           (value (-> parameters :auth-header :oauth-version))))

(def example-parameters
  {:auth-header 
   {
    :realm                  "http://sp.example.com/" 
    :oauth-consumer-key     "0685bd9184jfhq22"
    :oauth-token            "ad180jjd733klru7"
    :oauth-signature-method "HMAC-SHA1"
    :oauth-signature        "wOJIO9A2W5mFwDgiDvZbTSMK/PY="
    :oauth-timestamp        "1423786932"
    :oauth-nonce            "4572616e48616d6d65724c61686176"
    :oauth-version          "1.0"
    }
   })

(defn- example-parameters-with[replacement-oauth-parameters]
  (let [original-values (-> example-parameters :auth-header)]
    {:auth-header (into original-values replacement-oauth-parameters)}))

(def debug? (= "ON" (System/getenv "LOUD")))

(defn- must-contain[text expected] (is (.contains text expected) (str "Expected <" text "> to include <" expected ">")))
(defn- must-not-contain[text expected] (is (not (.contains text expected)) (str "Expected <" text "> to exclude <" expected ">")))

(deftest normalizing-request-parameters
  (let [result (signature-base-string example-parameters)]
    (testing "that it omits realm"
      (is (= false (.contains result "realm="))))
    
    (testing "that it includes :oauth_consumer_key"
      (must-contain result "oauth_consumer_key%3D0685bd9184jfhq22"))

    (testing "that it includes :oauth_token"
      (must-contain result "oauth_token%3Dad180jjd733klru7"))

    (testing "that it includes :oauth_signature_method"
      (must-contain result "oauth_signature_method%3DHMAC-SHA1"))

    (testing "that it excludes :oauth_signature"
      (must-not-contain result "oauth_signature%7DwOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"))

    (testing "that it includes :oauth_timestamp"
      (must-contain result "oauth_timestamp%3D1423786932"))

    (testing "that it includes :oauth_nonce"
      (must-contain result "oauth_nonce%3D4572616e48616d6d65724c61686176"))

    (testing "that it includes :oauth_version"
      (must-contain result "oauth_version%3D1.0"))))

(deftest request-parameter-values-are-parameter-encoded
  (let [result (signature-base-string (example-parameters-with { :oauth-version "/OJI O9A2W5mFwDgiDvZbTSMK/PY=" }))]
    (testing "for example a fictional oauth_version"
      (must-contain result "oauth_version%3D%2FOJI%20O9A2W5mFwDgiDvZbTSMK%2FPY%3D"))))

(deftest request-parameter-values-may-be-empty-and-are-still-included
  (let [result (signature-base-string (example-parameters-with { :oauth-version "" }))]
    (testing "for example a fictional empty oauth_version"
      (must-contain result "oauth_version%3D"))))

;; TEST: parameters are separated by ampersands
;; TEST: it EXCLUDES oauth_signature
