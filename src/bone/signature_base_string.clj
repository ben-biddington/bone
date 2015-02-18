(ns bone.signature-base-string
  (:gen-class)
  (:import java.lang.String)
  (:import java.net.URI)
  (:require [ring.util.codec :refer :all]))

(defstruct parameter :name :value) 

(defn- %[what] (if (nil? what) "" (ring.util.codec/url-encode what)))
(def ^{:private true} ignored-parameter-names #{"realm" "oauth_signature"})
(def ^{:private true} ampersand "&")
(def ^{:private true} url-encoded-ampersand (% ampersand))
(defn- param[name-and-value] (struct parameter (get name-and-value :name) (get name-and-value :value)))

(defn- sort-by-key-and-value [parameters]       (sort-by (juxt :name :value) parameters))
(defn- join-as-string        [param]            (str (% (:name param)) (% "=") (% (:value param))))
(defn- name-value-pairs      [parameters]       (map join-as-string parameters))
(defn- blacklisted?          [item]             (contains? ignored-parameter-names (:name item)))
(defn- white-list            [parameters]       (map param (filter (complement blacklisted?) parameters)))
(defn- combine               [name-value-pairs] (clojure.string/join url-encoded-ampersand name-value-pairs))
(defn- normalize-earl        [url]
  (let [uri (URI. url)]
    (str (clojure.string/lower-case (.getScheme uri)) "://" (.getHost uri) (.getPath uri))))

(defn signature-base-string[args]
    (clojure.string/join ampersand
      (list 
        (-> :verb       args %)
        (-> :url        args normalize-earl %)
        (-> :parameters args white-list sort-by-key-and-value name-value-pairs combine))))

