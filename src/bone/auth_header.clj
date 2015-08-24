(ns bone.auth-header
  (:require [clojure.test :refer :all]
            [bone.support :refer :all]
            [bone.util :refer :all]
            [bone.signature :refer :all]
            [bone.auth-header :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.signature-base-string.support :refer :all]))

(defn- to-param[hash]
  (let [[name value] hash]
    (struct parameter name value)))

(defn- params-for[verb url parameters credential]
  {
   :verb                      (or verb (fail ":verb is required"))
   :url                       (or url (fail ":url is required"))
   :parameters (concat
                (list
                 (param "oauth_consumer_key"     (:consumer-key credential))
                 (param "oauth_token"            (:token-key credential))
                 (param "oauth_timestamp"        "1191242096")
                 (param "oauth_nonce"            "kllo9940pd9333jh")
                 (param "oauth_signature_method" "HMAC-SHA1")
                 (param "oauth_version"          "1.0"))
                (map to-param parameters))})

(defn- secret[credential]
  (format "%s&%s" (% (:consumer-secret credential)) (% (:token-secret credential))))

(defn sign[credential opts]
  (let [{url :url verb :verb parameters :parameters} opts]
    (let [base-string (signature-base-string (params-for verb url parameters credential))]
      (let [signature (hmac-sha1-sign base-string (secret credential))]
        (format "Authorization: OAuth, oauth_consumer_key=\"%s\", oauth_token=\"%s\", oauth_signature=\"%s\"",
                (% (:consumer-key credential))
                (% (:token-key credential))
                (% signature))))))