(ns bone.signature-base-string.example
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.support :refer :all]
            [bone.signature-base-string.support :refer :all]
            [ring.util.codec :refer :all]))

; <http://oauth.net/core/1.0a/#anchor13>
;; The Signature Base String is a consistent reproducible concatenation of the request elements into a single string. 
;; The string is used as an input in hashing or signing algorithms. 
;; The HMAC-SHA1 signature method provides both a standard and an example of using the Signature Base String with a signing algorithm to generate signatures. 
;; All the request parameters MUST be encoded as described in Parameter Encoding;; prior to constructing the Signature Base String.

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

;; TEST: it concatenates with & even when pieces are empty, see section 9.1.3
