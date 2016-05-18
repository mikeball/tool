(defproject dep-retriever "0.1.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.4"]
                 [com.cemerick/pomegranate "0.3.1"]]
  :main deps.core
  :aot [deps.core]
  :profiles {:uberjar {:aot :all}})
