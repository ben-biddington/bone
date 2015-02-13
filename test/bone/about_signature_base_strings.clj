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

(defn- signature-base-string[parameters]
  (str 
   "oauth_consumer_key="      (% (-> parameters :auth-header :oauth-consumer-key))
   "oauth_token="             (% (-> parameters :auth-header :oauth-token))
   "oauth_signature_method="  (% (-> parameters :auth-header :oauth-signature-method))
   "oauth_signature="         (% (-> parameters :auth-header :oauth-signature))
   ))

(def example-parameters
  {:auth-header 
   {
    :realm                  "http://sp.example.com/" 
    :oauth-consumer-key     "0685bd9184jfhq22"
    :oauth-token            "ad180jjd733klru7"
    :oauth-signature-method "HMAC-SHA1"
    :oauth-signature        "wOJIO9A2W5mFwDgiDvZbTSMK/PY="
    }
   })

(def debug? (= "ON" (System/getenv "LOUD")))

(deftest normalizing-request-parameters
  (let [result (signature-base-string example-parameters)]
    (testing "that it omits realm"
      (is (= false (.contains result "realm="))))
    
    (testing "that it includes :oauth_consumer_key"
      (is (= true (.contains result "oauth_consumer_key=0685bd9184jfhq22"))))

    (testing "that it includes :oauth_token"
      (is (= true (.contains result "oauth_token=ad180jjd733klru7"))))

    (testing "that it includes :oauth_signature_method"
      (is (= true (.contains result "oauth_signature_method=HMAC-SHA1"))))

    (testing "that it includes :oauth_signature (this also shows we are URL encoding)"
      (is (= true (.contains result "oauth_signature=wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"))))

    ))
