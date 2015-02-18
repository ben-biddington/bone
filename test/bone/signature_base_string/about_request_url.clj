(ns bone.signature-base-string.about-request-url
  (:import java.lang.String)
  (:require [clojure.test :refer :all]
            [bone.signature-base-string :refer :all]
            [bone.signature-base-string.support :refer :all]
            [ring.util.codec :refer :all]))

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

