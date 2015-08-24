(ns bone.util
  (:import java.lang.String)
  (:import java.net.URI)
  (:require [ring.util.codec :refer :all]))

(defn %[what] (if (nil? what) "" (ring.util.codec/url-encode what)))