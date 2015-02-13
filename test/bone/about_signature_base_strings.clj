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

(defn- q[what] (str "\"" what "\""))

(defn- value[what] 
  (q (% (
    if (nil? what) 
    ""
    what))))

(defn- signature-base-string[parameters]
  (str 
   "oauth_consumer_key="      (value (-> parameters :auth-header :oauth-consumer-key))
   "oauth_token="             (value (-> parameters :auth-header :oauth-token))
   "oauth_signature_method="  (value (-> parameters :auth-header :oauth-signature-method))
   "oauth_signature="         (value (-> parameters :auth-header :oauth-signature))
   "oauth_timestamp="         (value (-> parameters :auth-header :oauth-timestamp))
   "oauth_nonce="             (value (-> parameters :auth-header :oauth-nonce))
   "oauth_version="           (value (-> parameters :auth-header :oauth-version))
   ))

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

(defn- must-contain[text expected] (is (.contains text expected)))

(deftest normalizing-request-parameters
  (let [result (signature-base-string example-parameters)]
    (testing "that it omits realm"
      (is (= false (.contains result "realm="))))
    
    (testing "that it includes :oauth_consumer_key"
      (must-contain result "oauth_consumer_key=\"0685bd9184jfhq22\""))

    (testing "that it includes :oauth_token"
      (must-contain result "oauth_token=\"ad180jjd733klru7\""))

    (testing "that it includes :oauth_signature_method"
      (must-contain result "oauth_signature_method=\"HMAC-SHA1\""))

    (testing "that it includes :oauth_signature"
      (must-contain result "oauth_signature=\"wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D\""))

    (testing "that it includes :oauth_timestamp"
      (must-contain result "oauth_timestamp=\"1423786932\""))

    (testing "that it includes :oauth_nonce"
      (must-contain result "oauth_nonce=\"4572616e48616d6d65724c61686176\""))

    (testing "that it includes :oauth_version"
      (must-contain result "oauth_version=\"1.0\""))))

(deftest request-parameter-values-are-parameter-encoded
  (let [result (signature-base-string (example-parameters-with { :oauth-version "/OJI O9A2W5mFwDgiDvZbTSMK/PY=" }))]
    (testing "for example a fictional oauth_version"
      (must-contain result "oauth_version=\"%2FOJI%20O9A2W5mFwDgiDvZbTSMK%2FPY%3D\""))))

;; TEST: parameter values may be empty -- they must still be included
;;       + what about whitespace?
