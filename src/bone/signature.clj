(ns bone.signature
  (:import java.lang.String)
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec))

  (:require [bone.support :refer :all]))

;; http://oauth.net/core/1.0a/#signing_process
;; HMAC-SHA1: http://oauth.net/core/1.0a/#RFC2104

(defn- %64 [what] (org.apache.commons.codec.binary.Base64/encodeBase64 what))
(defn- utf-8-str [what] (String. what "UTF-8"))
(defn- hmac [^String data ^String key]
  (let [hmac-sha1 "HmacSHA1"
        secret (SecretKeySpec. (.getBytes key) hmac-sha1)
        mac (doto (Mac/getInstance hmac-sha1) (.init secret))]

    (utf-8-str (%64 (.doFinal mac (.getBytes data))))))

(defn hmac-sha1-sign[base_string, secret] (hmac base_string secret))
