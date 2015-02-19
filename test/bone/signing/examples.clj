(ns bone.signing.examples
  (:import java.lang.String)
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec))

  (:require [clojure.test :refer :all]))

;; http://oauth.net/core/1.0a/#signing_process
;; HMAC-SHA1: http://oauth.net/core/1.0a/#RFC2104

;(defn- hmac [^String key ^String data]
;  (let [hmac-sha1 "HmacSHA1"
;        signing-key (SecretKeySpec. (.getBytes key) hmac-sha1)
;        mac (doto (Mac/getInstance hmac-sha1) (.init signing-key))]
;    (String. (org.apache.commons.codec.binary.Base64/encodeBase64 
;              (.doFinal mac (.getBytes data)))
;             "UTF-8")))

(defn hmac-sha1-sign[base_string, secret])

(deftest signing-with-hmac-sha1
  (testing "for example"
    (let [
          signature-base-string (str "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&file%3Dvacation.jpg%26" 
                                     "oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26" 
                                     "oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal")
          secret "kd94hf93k423kf44&pfkkdhi9sl3r4s00"
          result (hmac-sha1-sign signature-base-string secret)]
          )))
