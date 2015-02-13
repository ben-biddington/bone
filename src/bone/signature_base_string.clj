(ns bone.signature-base-string
  (:gen-class)
  (:import java.lang.String)
  (:require [ring.util.codec :refer :all]))

(defn- %[what] (ring.util.codec/url-encode what))
(def ^{:private true} ignored-parameter-names #{"realm" "oauth_signature"})
(def ^{:private true} ampersand "&")
(def ^{:private true} url-encoded-ampersand (% "&"))

(defn- sort-by-key-and-value[parameters] (into (sorted-map) parameters))
(defn- join-as-string[param] (str (%(key param)) (% "=") (% (val param))))
(defn- name-value-pairs[parameters] (map join-as-string parameters))
(defn- blacklisted? [item] (contains? ignored-parameter-names (key item)))
(defn- white-list[parameters] (filter (complement blacklisted?) parameters))
(defn- combine[name-value-pairs] (clojure.string/join url-encoded-ampersand name-value-pairs))
(defn- sorted[parameters] (sort-by-key-and-value (white-list parameters)))

(defn signature-base-string[args]
  (let [sorted-params (sorted (:parameters args))]
    (clojure.string/join ampersand
      (list 
        (% (:verb args))
        (% (:url args))
        (-> sorted-params name-value-pairs combine)))))
