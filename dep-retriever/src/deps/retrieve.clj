(ns deps.retrieve
  (:require
    [cemerick.pomegranate.aether :as aether]))

(def repos
  (merge aether/maven-central
         {"clojars" "http://clojars.org/repo"}))

(defn transfer-listener
  [{:keys [type error resource] :as info}]
  (let [{:keys [name repository]} resource]
    (binding [*out* *err*]
      (case type
        :started (println "Retrieving" name "from" repository)
        :corrupted (when error (println (.getMessage error)))
        nil))))

(defn retrieve [coords]
  (aether/dependency-files
    (aether/resolve-dependencies
      :coordinates coords
      :repositories repos
      :transfer-listener transfer-listener)))
