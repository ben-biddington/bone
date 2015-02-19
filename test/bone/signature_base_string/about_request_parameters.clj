(ns bone.signature-base-string.about-request-parameters
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.support :refer :all]
            [bone.signature-base-string.support :refer :all]
            [ring.util.codec :refer :all]))

(def example-parameters
   {
    :verb                      "GET"
    :url                       "http://sp.example.com/" 
    :parameters (list
      (param "realm"                  "http://sp.example.com/")
      (param "oauth_consumer_key"     "0685bd9184jfhq22")
      (param "oauth_token"            "ad180jjd733klru7")
      (param "oauth_signature_method" "HMAC-SHA1")
      (param "oauth_signature"        "wOJIO9A2W5mFwDgiDvZbTSMK/PY=")
      (param "oauth_timestamp"        "1423786932")
      (param "oauth_nonce"            "4572616e48616d6d65724c61686176")
      (param "oauth_version"          "1.0"))})

(defn- example-parameters-with[replacements] (merge-with concat example-parameters replacements)) ;; does not seem to merge sub-maps

(deftest normalizing-request-parameters
  (let [result (signature-base-string example-parameters)]
    (testing "that it exludes realm"
      (must-not-contain result "realm%3D"))
    
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
  (let [input (example-parameters-with { :parameters (list (param "oauth_version" "/OJI O9A2W5mFwDgiDvZbTSMK/PY=")) })]
    (let [result (signature-base-string input)]
    (testing "for example a fictional oauth_version"
      (must-contain result "oauth_version%3D%2FOJI%20O9A2W5mFwDgiDvZbTSMK%2FPY%3D")))))

(deftest request-parameter-values-may-be-empty-and-are-still-included
  (let [result (signature-base-string (example-parameters-with { :parameters (list (param "oauth_version" "")) }))]
    (testing "for example a fictional empty oauth_version"
      (must-contain result "oauth_version%3D"))))

(deftest request-parameters-with-the-same-name-are-sorted-by-name-and-value
  (let [parameters {
    :verb                      "GET"
    :url                       "http://photos.example.net/photos"
    :parameters (list 
      (param "f"                      "50")
      (param "f"                      "25"))}]
  
  (let [result (signature-base-string parameters)]
    (testing "for example (2) values for \"f\""
      (must-contain result "f%3D25%26f%3D50")))))
