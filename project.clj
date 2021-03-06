(defproject bone "0.9.0-SNAPSHOT"
  :description "Simple OAuth for clojure"
  :url "https://github.com/ben-biddington/bone"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [ring/ring-codec "1.0.0"]
                 [clj-http "2.0.0"]]
  :main ^:skip-aot bone.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
