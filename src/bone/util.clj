(ns bone.util
  (:import java.lang.String)
  (:import java.net.URI)
  (:require [clj-http.util :refer :all :as http]))

(defn %[what] (if (nil? what) "" (http/url-encode what)))

(defn fail[message & args]
  (throw
   (Exception. (apply format message args))))

(defn when-nil[what do-this]
  (when (nil? what)
    (do-this)))