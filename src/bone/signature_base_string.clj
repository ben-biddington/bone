(ns bone.signature-base-string
  (:import java.lang.String)
  (:import java.net.URI)
  (:require [ring.util.codec :refer :all]
            [bone.util :refer :all]))

(defstruct parameter :name :value) 

(defn param[name,value] (struct parameter name value))

(def ^{:private true} ignored-parameter-names #{"realm" "oauth_signature"})
(def ^{:private true} ignored-ports #{80,443,-1})
(def ^{:private true} ampersand "&")
(def ^{:private true} url-encoded-ampersand (% ampersand))
(defn- param[name-and-value] (struct parameter (get name-and-value :name) (get name-and-value :value)))
(defn- down-case             [what] (clojure.string/lower-case (if (nil? what) "" what)))
(defn- up-case               [what] (clojure.string/upper-case (if (nil? what) "" what)))
(defn- sort-by-key-and-value [parameters]       (sort-by (juxt :name :value) parameters))
(defn- join-as-string        [param]            (str (-> param :name %) "=" (-> param :value %)))
(defn- name-value-pairs      [parameters]       (map join-as-string parameters))
(defn- blacklisted?          [item]             (contains? ignored-parameter-names (:name item)))
(defn- white-list            [parameters]       (map param (filter (complement blacklisted?) parameters)))
(defn- combine               [name-value-pairs] (clojure.string/join ampersand name-value-pairs))

(defn- port-string           [uri]
  (let [port (.getPort uri)]
    (if (contains? ignored-ports port) "" (str ":" port))))

(defn- normalize-earl        [url]
  (if (nil? url) 
    ""
    (let [uri (URI. url)]
      (str (down-case (.getScheme uri)) "://" (down-case (.getHost uri)) (port-string uri) (.getPath uri)))))

(defn signature-base-string[args]
    (clojure.string/join ampersand
      (list 
        (-> :verb       args up-case %)
        (-> :url        args normalize-earl %)
        (-> :parameters args white-list sort-by-key-and-value name-value-pairs combine %))))

