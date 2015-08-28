(ns bone.signing.examples
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.support :refer :all]
            [bone.signature :refer :all]
            [bone.auth-header :refer :all :as header]))

;; http://oauth.net/core/1.0a/#signing_process
;; HMAC-SHA1: http://oauth.net/core/1.0a/#RFC2104

(deftest signing-with-hmac-sha1
  (testing "for example" ;; See <http://oauth.net/core/1.0a/#RFC2104>
    (let [
          signature-base-string (str "GET&http%3A%2F%2Fphotos.example.net%2Fphotos&file%3Dvacation.jpg%26" 
                                     "oauth_consumer_key%3Ddpf43f3p2l4k3l03%26oauth_nonce%3Dkllo9940pd9333jh%26oauth_signature_method%3DHMAC-SHA1%26" 
                                     "oauth_timestamp%3D1191242096%26oauth_token%3Dnnch734d00sl2jdk%26oauth_version%3D1.0%26size%3Doriginal")
          secret "kd94hf93k423kf44&pfkkdhi9sl3r4s00"
          result (hmac-sha1-sign signature-base-string secret)]

      (must-equal "tR3+Ty81lMeYAr/Fid0kMTYa/WM=" result))))

(def ^{:private true} credential
     {
      :consumer-key "dpf43f3p2l4k3l03" :consumer-secret "kd94hf93k423kf44"
      :token-key    "nnch734d00sl2jdk" :token-secret    "pfkkdhi9sl3r4s00"})

(deftest creating-authorization-headers
  (let [opts {:verb "GET" :url "http://photos.example.net/photos" :parameters { "file" "vacation.jpg" "size" "original"} :timestamp-fn (fn[] 1191242096) :nonce-fn (fn[] "kllo9940pd9333jh")}]
    (let [result (header/sign credential opts)]

      (testing "that is conforms to the right pattern -- starts with 'OAuth'"
               (is (not (nil? (re-matches #"^OAuth.+" result)))))
      
      (testing "that it contains the consumer key"
               (is (.contains result "oauth_consumer_key=\"dpf43f3p2l4k3l03\"")))

      (testing "that it contains the oauth token"
               (is (.contains result "oauth_token=\"nnch734d00sl2jdk\"")))

      (testing "that it contains the signature method"
               (is (.contains result "oauth_signature_method=\"HMAC-SHA1\"")))
      
      (testing "that it contains the signature"
               (is (.contains result "oauth_signature=\"tR3%2BTy81lMeYAr%2FFid0kMTYa%2FWM%3D\"")))

      (testing "that it contains the timestamp"
               (is (.contains result "oauth_timestamp=\"1191242096\"")))

      (testing "that it contains the nonce"
               (is (.contains result "oauth_nonce=\"kllo9940pd9333jh\"")))

      (testing "that it contains the version"
               (is (.contains result "oauth_version=\"1.0\"")))
      
      )))