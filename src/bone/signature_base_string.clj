(ns bone.signature-base-string
  (:gen-class)
  (:import java.lang.String)
  (:require [ring.util.codec :refer :all]))

(defn- %[what] (ring.util.codec/url-encode what))

(defn- sort-by-key-and-value[parameters] (into (sorted-map) parameters))

(defn- join-as-string[param] (str (%(key param)) (% "=") (% (val param))))

(def ^{:private true} ignored-parameter-names #{"verb" "url" "realm" "oauth_signature"})

(defn- blacklisted? [item] (contains? ignored-parameter-names (key item)))

(defn- white-list[parameters] (filter (complement blacklisted?) parameters))

(def ^{:private true} ampersand "&")

(defn signature-base-string[parameters]
  (let [sorted-params (sort-by-key-and-value (white-list (:auth-header parameters)))]
    (clojure.string/join ampersand
      (list 
        (% (get-in parameters [:auth-header "verb"]))
        (% (get-in parameters [:auth-header "url"]))
        (clojure.string/join (% "&") (map join-as-string sorted-params))))))
