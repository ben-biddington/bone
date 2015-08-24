(ns bone.auth-header
  (:require [clojure.test :refer :all]
            [bone.support :refer :all]
            [bone.signature :refer :all]
            [bone.auth-header :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.signature-base-string.support :refer :all]))

(defn- params-for[verb url credential]
  {
    :verb                      verb
    :url                       url
    :parameters (list 
      (param "oauth_consumer_key"     (:consumer-key credential))
      (param "oauth_token"            "nnch734d00sl2jdk")
      (param "oauth_timestamp"        "1191242096")
      (param "oauth_nonce"            "kllo9940pd9333jh")
      (param "oauth_signature_method" "HMAC-SHA1")
      (param "oauth_version"          "1.0")
      (param "file"                   "vacation.jpg")
      (param "size"                   "original"))})

(defn sign[credential opts]
  (let [{url :url verb :verb} opts]
    (let [base-string (signature-base-string (params-for verb url credential))])
    ;; make a base string
    ;; sign it
    ;; assemble header
    (format "Authorization: OAuth, oauth_consumer_key=\"%s\", oauth_token=\"%s\"",
            (:consumer-key credential) (:token-key credential))))