(ns deps.core
  (:require
    [clojure.edn :as edn]
    [clojure.string :as string]
    [clojure.tools.cli :refer [parse-opts]]
    [deps.retrieve :refer [retrieve]])
  (:gen-class))

(def cli-options
  [["-h" "--help"]])

(defn usage [options-summary]
  (string/join "\n"
    [""
     "Retrieve dependencies from the given dependencies vector."
     ""
     "Results:"
     "  stderr: download progress"
     "  stdout: resulting JAR filepaths (one per line)"
     ""
     "Options:"
     options-summary
     ""]))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join "\n" errors)))

(defn exit [status msg]
  (binding [*out* *err*]
    (println msg))
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]

    (when (or (:help options) (not (seq arguments)))
      (exit 1 (usage summary)))

    (when errors
      (exit 1 (error-msg errors)))

    (->> (first arguments)  ;; dependencies vector (string)
         (edn/read-string)  ;; dependencies vector (data)
         (retrieve)         ;; jar files
         (map str)          ;; jar files (path strings)
         (string/join "\n")
         (println))))
