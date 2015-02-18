(ns bone.signature-base-string.about-signature-base-strings
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.signature-base-string.support :refer :all]
            [ring.util.codec :refer :all]))

; <http://oauth.net/core/1.0a/#anchor13>
;; The Signature Base String is a consistent reproducible concatenation of the request elements into a single string. 
;; The string is used as an input in hashing or signing algorithms. 
;; The HMAC-SHA1 signature method provides both a standard and an example of using the Signature Base String with a signing algorithm to generate signatures. 
;; All the request parameters MUST be encoded as described in Parameter Encoding;; prior to constructing the Signature Base String.
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
      (param "oauth_version"          "1.0"))
    })

(defn- example-parameters-with[replacements] (merge-with concat example-parameters replacements)) ;; does not seem to merge sub-maps

(deftest request-verb 
  (let [result (signature-base-string { :verb "get" })]
      (testing "that it upper-cases verb"
        (must-not-contain result "get")
        (must-contain result "GET"))))

(deftest request-url
  (let [parameters {:url "HTTP://Example.com:80/resource#example-fragment?id=123" }]
    (let [result (signature-base-string parameters)]
      (testing "that it EXCLUDES the query string entirely"
        (must-not-contain result "resource%3Fid%3D123"))
      
      (testing "that it EXCLUDES the fragment entirely"
        (must-not-contain result "example-fragment"))

      (testing "that it downcases the scheme"
        (must-not-contain result "HTTP")
        (must-contain     result "http"))

      (testing "that it downcases the authority"
        (must-not-contain result "Example.com")
        (must-contain     result "example.com"))

      (testing "that it omits port 80"
        (must-not-contain result "80"))))

    (let [result (signature-base-string { :url "http://example.com:443" })]
      (testing "that it omits port 443 (and the colon)"
        (must-not-contain result "%3A443")))

    (let [result (signature-base-string { :url "http://example.com:1337" })]
      (testing "that it includes any other port, lke 1337 for example"
        (must-contain result "http%3A%2F%2Fexample.com%3A1337"))))

(deftest for-example ;; <http://oauth.net/core/1.0a/#sig_base_example>
  (let [parameters {
    :verb                      "GET"
    :url                       "http://photos.example.net/photos"
    :parameters (list 
      (param "oauth_consumer_key"     "dpf43f3p2l4k3l03")
      (param "oauth_token"            "nnch734d00sl2jdk")
      (param "oauth_timestamp"        "1191242096")
      (param "oauth_nonce"            "kllo9940pd9333jh")
      (param "oauth_signature_method" "HMAC-SHA1")
      (param "oauth_version"          "1.0")
      (param "file"                   "vacation.jpg")
      (param "size"                   "original"))}]

  (let [result (signature-base-string parameters)]
    (must-equal result (str "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&"
                "file%3Dvacation.jpg%26oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26" 
                "oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26" 
                "oauth_version%3D1.0%26size%3Doriginal")))))

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

;; TEST: it concatenates with & even when pieces are empty, see section 9.1.3
