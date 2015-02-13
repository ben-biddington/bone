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

(defn- %[what] (ring.util.codec/url-encode what))

(defn- sort-by-key-and-value[parameters] (into (sorted-map) parameters))

(defn- join-as-string[param] (str (%(key param)) (% "=") (% (val param))))

(def ^{:private true} ignored-parameter-names #{"realm" "oauth_signature"})

(defn- blacklisted? [item] (contains? ignored-parameter-names (key item)))

(defn- white-list[parameters] (filter (complement blacklisted?) parameters))

(defn- signature-base-string[parameters]
  (let [sorted-params (sort-by-key-and-value (white-list (:auth-header parameters)))]
    (clojure.string/join (% "&") (map join-as-string sorted-params))))

(def example-parameters
  {:auth-header 
   {
    "realm"                  "http://sp.example.com/" 
    "oauth_consumer_key"     "0685bd9184jfhq22"
    "oauth_token"            "ad180jjd733klru7"
    "oauth_signature_method" "HMAC-SHA1"
    "oauth_signature"        "wOJIO9A2W5mFwDgiDvZbTSMK/PY="
    "oauth_timestamp"        "1423786932"
    "oauth_nonce"            "4572616e48616d6d65724c61686176"
    "oauth_version"          "1.0"
    }
   })

(defn- example-parameters-with[replacement-oauth-parameters]
  (let [original-values (-> example-parameters :auth-header)]
    {:auth-header (into original-values replacement-oauth-parameters)}))

(def debug? (= "ON" (System/getenv "LOUD")))

(defn- must-contain[text expected] (is (.contains text expected) (str "Expected <" text "> to include <" expected ">")))
(defn- must-not-contain[text expected] (is (not (.contains text expected)) (str "Expected <" text "> to exclude <" expected ">")))

(deftest for-example ;; <http://oauth.net/core/1.0a/#sig_base_example>
  (let [parameters (example-parameters-with { 
    "oauth_consumer_key" "dpf43f3p2l4k3l03" 
    "oauth_token"        "nnch734d00sl2jdk"
    "oauth_timestamp"    "1191242096"
    "oauth_nonce"        "kllo9940pd9333jh"
    "file"               "vacation.jpg"
    "size"               "original"})]
  (let [result (signature-base-string parameters)]
    (is (= (str "file%3Dvacation.jpg%26oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26" 
                "oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26" 
                "oauth_version%3D1.0%26size%3Doriginal") result))
    ))
)

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
  (let [result (signature-base-string (example-parameters-with { "oauth_version" "/OJI O9A2W5mFwDgiDvZbTSMK/PY=" }))]
    (testing "for example a fictional oauth_version"
      (must-contain result "oauth_version%3D%2FOJI%20O9A2W5mFwDgiDvZbTSMK%2FPY%3D"))))

(deftest request-parameter-values-may-be-empty-and-are-still-included
  (let [result (signature-base-string (example-parameters-with { "oauth_version" "" }))]
    (testing "for example a fictional empty oauth_version"
      (must-contain result "oauth_version%3D"))))


;; TEST: parameters must be sorted by name AND value
