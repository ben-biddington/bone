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

(defn- params-for[verb url timestamp nonce parameters credential]
  {
   :verb                      (or verb (fail ":verb is required"))
   :url                       (or url (fail ":url is required"))
   :parameters (concat
                (list
                 (param "oauth_consumer_key"     (:consumer-key credential))
                 (param "oauth_token"            (:token-key credential))
                 (param "oauth_timestamp"        (or timestamp (fail "timestamp is required")))
                 (param "oauth_nonce"            (or nonce (fail "nonce is required")))
                 (param "oauth_signature_method" "HMAC-SHA1")
                 (param "oauth_version"          "1.0"))
                (map to-param parameters))})

(defn- secret[credential]
  (format "%s&%s" (% (:consumer-secret credential)) (% (:token-secret credential))))

(defn- nonce-and-timestamp[opts]
  (let [{timestamp-fn :timestamp-fn nonce-fn :nonce-fn} opts]
    (when (nil? timestamp-fn) (fail "timestamp-fn is required (it is a function that returns timestamps)"))
    (when (nil? nonce-fn) (fail "nonce-fn is required (it is a function that returns nonces)"))

    [(str (apply nonce-fn [])) (str (apply timestamp-fn []))]))

(defn sign[credential opts]
  (let [{url :url verb :verb parameters :parameters timestamp-fn :timestamp-fn nonce-fn :nonce-fn} opts]
    (let [[nonce timestamp] (nonce-and-timestamp opts)]
      (let [signature (hmac-sha1-sign (signature-base-string (params-for verb url timestamp nonce parameters credential)) (secret credential))]
        (format "Authorization: OAuth, oauth_consumer_key=\"%s\", oauth_token=\"%s\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"%s\", oauth_timestamp=\"%s\", oauth_nonce=\"%s\"",
                (% (:consumer-key credential))
                (% (:token-key credential))
                (% signature)
                (% timestamp)
                (% nonce))))))